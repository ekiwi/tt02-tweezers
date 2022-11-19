import chisel3._
import chisel3.util._

class AmaranthAsyncFifo extends TweezersDesign with RequireAsyncReset {
  val Width = 3

  val fifo = Module(new AsyncFifo(UInt(Width.W), 4))

  // connect inputs
  fifo.read.clock := io.inputs(0).asClock
  // the fifo is reset exclusively from the write domain
  fifo.write.enq.valid := io.inputs(1)
  fifo.read.deq.ready := io.inputs(2)
  require(Width + 3 <= io.inputs.length, f"Only ${io.inputs.length} input bits are available")
  fifo.write.enq.bits := Cat(io.inputs.drop(3).take(Width))

  // connect outputs
  io.outputs := DontCare
  io.outputs(0) := fifo.write.enq.ready
  io.outputs(1) := fifo.read.deq.valid
  io.outputs.drop(2).zip(fifo.read.deq.bits.asBools).foreach{ case (a,b) => a := b }
}

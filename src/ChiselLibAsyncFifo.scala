import chisel3._
import chisel3.util._

class ChiselLibAsyncFifo extends TweezersDesign with RequireAsyncReset {
  val Width = 2

  val fifo = Module(new DCAsyncFifo(UInt(Width.W), 4))

  // connect inputs
  fifo.io.enqClock := clock
  fifo.io.enqReset := reset
  fifo.io.deqClock := io.inputs(0).asClock
  fifo.io.deqReset := io.inputs(1).asAsyncReset
  fifo.io.enq.valid := io.inputs(2)
  fifo.io.deq.ready := io.inputs(3)
  require(Width + 4 <= io.inputs.length, f"Only ${io.inputs.length} input bits are available")
  fifo.io.enq.bits := Cat(io.inputs.drop(4).take(Width))

  // connect outputs
  io.outputs := DontCare
  io.outputs(0) := fifo.io.enq.ready
  io.outputs(1) := fifo.io.deq.valid
  io.outputs.drop(2).zip(fifo.io.deq.bits.asBools).foreach{ case (a,b) => a := b }
}

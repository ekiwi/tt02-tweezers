import chisel3._
import chisel3.util._



/** Instantiates two different async clock domain crossing fifos for testing them both.
 *  Inputs are shared and drive both FIFOs.
 * */
class DualFifoDesign(depth: Int = 8, width: Int = 2) extends TweezersDesign with RequireAsyncReset {
  val fifo = Module(new AsyncFifo(UInt(width.W), depth))
  val dcFifo = Module(new DCAsyncFifo(UInt(width.W), depth))

  // connect inputs
  // the DC fifo has explicit clock inputs
  dcFifo.io.enqClock := clock
  dcFifo.io.enqReset := reset
  // the other fifo has reset and clock on the write side connected by default!
  dcFifo.io.deqClock := io.inputs(0).asClock
  fifo.read.clock := io.inputs(0).asClock
  dcFifo.io.deqReset := io.inputs(1).asAsyncReset
  // the other fifo uses the write reset to reset both sides!
  fifo.write.enq.valid := io.inputs(2)
  dcFifo.io.enq.valid := io.inputs(2)
  fifo.read.deq.ready := io.inputs(3)
  dcFifo.io.deq.ready := io.inputs(3)
  require(width + 4 <= io.inputs.length, f"Only ${io.inputs.length} input bits are available")
  fifo.write.enq.bits := Cat(io.inputs.drop(4).take(width))
  dcFifo.io.enq.bits := Cat(io.inputs.drop(4).take(width))

  // connect outputs
  io.outputs := DontCare
  require((2 + width) * 2 <= io.outputs.length, f"Only ${io.outputs.length} output bits are available")
  io.outputs(0) := fifo.write.enq.ready
  io.outputs(1) := fifo.read.deq.valid
  io.outputs.drop(2).zip(fifo.read.deq.bits.asBools).foreach { case (a, b) => a := b }
  io.outputs(0 + 2 + width) := dcFifo.io.enq.ready
  io.outputs(1 + 2 + width) := dcFifo.io.deq.valid
  io.outputs.drop(2 + 2 + width).zip(dcFifo.io.deq.bits.asBools).foreach{ case (a,b) => a := b }
}
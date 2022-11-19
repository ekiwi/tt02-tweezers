

import chisel3._
import chisel3.stage.ChiselStage

// template I/O and class for user module
class TweezersIO extends Bundle {
    val inputs = Input(Vec(6, Bool()))
    val outputs = Output(Vec(8, Bool()))
}

abstract class TweezersDesign extends Module {
    val io = IO(new TweezersIO)
}

// TinyTapeout Wrapper
class TinyTapeoutIO extends Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
}

/** top module with TinyTapeout compatible I/O names.
 * @note: this needs to be a raw module since the clock pin is not named "clock" */
class TweezersTop(createDesign: () => TweezersDesign) extends RawModule {
    val io = IO(new TinyTapeoutIO)
    // clock and reset mapping mapping
    val clock = io.in(0)
    val reset = io.in(1)
    val tweezers = withClockAndReset(clock.asClock, reset) {
        Module(createDesign())
    }
    tweezers.io.inputs.zip(io.in.asBools.drop(2)).foreach{ case (a,b) => a := b }
    io.out := tweezers.io.outputs.asUInt
}

object TweezersGenerator {
    private val DefaultArgs = Array("--target-dir", "src")
    private val DefaultFrequency = 1000
    private val Designs = Map(
        "uart" -> (() => new UartDesign(frequency = DefaultFrequency, baud = 300)),
        "async-fifo-lib" -> (() => new ChiselLibAsyncFifo),
    )
    def main(args: Array[String]): Unit = {
        // DefaultArgs are useful when launching from IntelliJ
        val aa = if(args.length > 0) args else DefaultArgs
        val design = Designs("async-fifo-lib")
        (new ChiselStage).emitSystemVerilog(new TweezersTop(design), aa)
    }
}


import chisel3._
import chisel3.stage.ChiselStage


class TweezersIO extends Bundle {
    val rx = Input(Bool())
    val tx = Output(Bool())
}

class Tweezers extends Module {
    val io = IO(new TweezersIO)
    io.tx := io.rx
}


// TinyTapeout Wrapper
class TinyTapeoutIO extends Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
}

/** top module with TinyTapeout compatible I/O names.
 * @note: this needs to be a raw module since the clock pin is not named "clock" */
class TweezersTop extends RawModule {
    val io = IO(new TinyTapeoutIO)
    // clock and reset mapping mapping
    val clock = io.in(0)
    val reset = io.in(1)
    val tweezers = withClockAndReset(clock.asClock, reset) {
        Module(new Tweezers)
    }
    tweezers.io.rx := io.in(2)
    val out = Wire(Vec(8, Bool()))
    out := DontCare
    out(0) := tweezers.io.tx
    io.out := out.asUInt
}

object TweezersGenerator {
    private val DefaultArgs = Array("--target-dir", "src")
    def main(args: Array[String]): Unit = {
        // DefaultArgs are useful when launching from IntelliJ
        val aa = if(args.length > 0) args else DefaultArgs
        (new ChiselStage).emitSystemVerilog(new TweezersTop, aa)
    }
}
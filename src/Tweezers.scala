

import chisel3._
import chisel3.stage.ChiselStage
import chisel.lib.uart._


case class TweezersConfig(frequency: Int, baud: Int)

class TweezersIO extends Bundle {
    val rx = Input(Bool())
    val tx = Output(Bool())
}

class Tweezers(conf: TweezersConfig) extends Module {
    val io = IO(new TweezersIO)

    // instantiate standard sender and receiver
    val rx = Module(new Rx(conf.frequency, conf.baud))
    val tx = Module(new Tx(conf.frequency, conf.baud))

    // increment any received character and send it out
    rx.io.channel <> tx.io.channel
    tx.io.channel.bits := rx.io.channel.bits + 1.U


    // connect UART RX and TX pins to the outside world
    io.tx := tx.io.txd
    rx.io.rxd := io.rx
}


// TinyTapeout Wrapper
class TinyTapeoutIO extends Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
}

/** top module with TinyTapeout compatible I/O names.
 * @note: this needs to be a raw module since the clock pin is not named "clock" */
class TweezersTop(conf: TweezersConfig) extends RawModule {
    val io = IO(new TinyTapeoutIO)
    // clock and reset mapping mapping
    val clock = io.in(0)
    val reset = io.in(1)
    val tweezers = withClockAndReset(clock.asClock, reset) {
        Module(new Tweezers(conf))
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
        val conf = TweezersConfig(
            frequency = 3000,
            baud = 300,
        )
        (new ChiselStage).emitSystemVerilog(new TweezersTop(conf), aa)
    }
}
import chisel3._
import chisel.lib.uart._



class UartDesign(frequency: Int, baud: Int) extends TweezersDesign {
  // instantiate standard sender and receiver
  val rx = Module(new Rx(frequency, baud))
  val tx = Module(new Tx(frequency, baud))

  // increment any received character and send it out
  rx.io.channel <> tx.io.channel
  tx.io.channel.bits := rx.io.channel.bits + 1.U


  // connect UART RX and TX pins to the outside world
  io.outputs := DontCare
  io.outputs(0) := tx.io.txd
  rx.io.rxd := io.inputs(0)
}

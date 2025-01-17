package instruction

import config.Config._
import conv.compute.CONV_STATE
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.regif._
import spinal.lib.bus.regif.AccessType._

class Instruction extends Component {
    val io = new Bundle {
        val regSData = slave(AxiLite4(log2Up(registerAddrSize), 32))
    }
    noIoPrefix()
    AxiLite4SpecRenamer(io.regSData)
    val bus = BusInterface(io.regSData, sizeMap = SizeMapping(0, registerAddrSize))
    val convStateReg = bus.newReg(doc = "卷积状态指令")
    val convControlReg = bus.newReg(doc = "卷积控制指令")
    val convState = convStateReg.field(4 bits, RO, doc = "卷积的状态").asInput()
    val convControl = convControlReg.field(4 bits, WO, doc = "卷积的控制指令").asOutput()

    val shapeStateReg = bus.newReg(doc = "shape状态指令")
    val shapeControlReg = bus.newReg(doc = "shape控制指令")
    val shapeState = shapeStateReg.field(4 bits, RO, doc = "shape的状态").asInput()
    val shapeControl = shapeControlReg.field(4 bits, WO, doc = "shape的控制指令").asOutput()

    val ins = Array.tabulate(6) { i => {
        def gen = {
            val reg = bus.newReg(doc = "Reg" + i)
            val instruction = reg.field(32 bits, WO, doc = "reg" + i).asOutput().setName("instruction" + i)
            instruction
        }

        gen
    }
    }
    //    (0 until 6).foreach(i => {
    //        val reg = bus.newReg(doc = "Reg" + i)
    //        val instruction = reg.field(32 bits, WO, doc = "reg" + i).asOutput().setName("instruction" + i)
    //    })

    val s = List("conv", "shape")
    val dma = Array.tabulate(2) { i => {
        def gen = {

            val writeAddrReg = bus.newReg(doc = "dma写地址")
            val writeAddr = writeAddrReg.field(32 bits, WO, doc = s(i) + " dma写地址").setName(s(i) + "writeAddr").asOutput()
            val writeLenReg = bus.newReg(doc = "dma写长度,不以字节为单位，实际长度")
            val writeLen = writeLenReg.field(32 bits, WO, doc = s(i) + " dma写长度").setName(s(i) + "writeLen").asOutput()
            var addr = List(writeAddr)
            var len = List(writeLen)
            val readAddrReg = bus.newReg(doc = "dma读地址")
            val readAddr = readAddrReg.field(32 bits, WO, doc = s(i) + " dma读地址").setName(s(i) + "readAddr").asOutput()
            val readLenReg = bus.newReg(doc = "dma读长度，不以字节为单位，实际长度")
            val readLen = readLenReg.field(32 bits, WO, doc = s(i) + " dma读长度").setName(s(i) + "readLen").asOutput()
            addr = addr.::(readAddr)
            len = len.::(readLen)
            if (i == 1) {
                val readAddrReg = bus.newReg(doc = "dma读地址")
                val readAddr = readAddrReg.field(32 bits, WO, doc = s(i) + " dma读地址1").setName(s(i) + "readAddr1").asOutput()
                val readLenReg = bus.newReg(doc = "dma读长度，不以字节为单位，实际长度")
                val readLen = readLenReg.field(32 bits, WO, doc = s(i) + " dma读长度").setName(s(i) + "readLen1").asOutput()
                addr = addr.::(readAddr)
                len = len.::(readLen)
            }
            addr = addr.reverse
            len = len.reverse
            List(addr,len)
        }

        gen
    }
    }
    //    (0 until 2).foreach(i => {
    //        val writeAddrReg = bus.newReg(doc = "dma写地址")
    //        val writeAddr = writeAddrReg.field(32 bits, WO, doc = s(i) + " dma写地址").setName(s(i) + "writeAddr").asOutput()
    //        if(i==1){
    //            val writeAddrReg = bus.newReg(doc = "dma写地址")
    //            val writeAddr = writeAddrReg.field(32 bits, WO, doc = s(i) + " dma写地址1").setName(s(i) + "writeAddr1").asOutput()
    //        }
    //        val readAddrReg = bus.newReg(doc = "dma读地址")
    //        val readAddr = readAddrReg.field(32 bits, WO, doc = s(i) + " dma读地址").setName(s(i) + "readAddr").asOutput()
    //    })

    bus.accept(HtmlGenerator("Reg.html", "Npu"))
}

object Instruction extends App {
    SpinalVerilog(new Instruction)
}

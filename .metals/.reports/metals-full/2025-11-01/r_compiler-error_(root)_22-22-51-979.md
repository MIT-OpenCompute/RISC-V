error id: 5814DA83A6F52728A3B2C4DC5411414E
file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
### java.lang.IndexOutOfBoundsException: -1 is out of bounds (min 0, max 2)

occurred in the presentation compiler.



action parameters:
uri: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
text:
```scala
// See README.md for license details.

package RISCV

import chisel3._
import chisel3.util._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage
import scala.math._
import os.read

class Main() extends Module {
    val io = IO(new Bundle {
        val instructions = Input(Vec(10, I32.W))
        val RegFileA_out = Output(UInt(32.W))
        val RegFileB_out = Output(UInt(32.W))
    })

    // set up program counter
    val PC = Module(new PC())
    // Boot address
    PC.io.pc_in := 0.U(32.W)

    val instruction = 

    // set up register files A and B. They will have identical contents so that we can do dual reads.
    val regFileA = Module(new Registers())
    val regFileB = Module(new Registers())

    // Provide safe/default connections for all register-file inputs so
    // there are no uninitialized sinks when a particular opcode path
    // doesn't drive them. We assign defaults first and override in
    // the opcode-specific when blocks below.
    regFileA.io.write_enable := false.B
    regFileA.io.write_addr := 0.U // default write address
    regFileA.io.read_addr := 0.U // default read address
    regFileA.io.in := 0.U

    regFileB.io.write_enable := false.B
    regFileB.io.write_addr := 0.U // default write address
    regFileB.io.read_addr := 0.U // default read address    
    regFileB.io.in := 0.U

    // io for observing register file outputs in testbench    
    io.RegFileA_out := 0.U
    io.RegFileB_out := 0.U

    // Fetch opcode from instruction to determine type of instruction
    val opcode = io.instruction(6,0) // 7 bits

    // Decode logic opcodes
    switch(opcode) {
        is ("b1111111".U(7.W)) // Print all registers instruction (not standard RISC-V, just for testing)
        {
            val i = io.instruction(11,7) // destination register, 5 bits
            regFileA.io.read_addr := i // set read address to rd to observe the written
            regFileB.io.read_addr := i
            regFileA.io.write_enable := false.B
            regFileB.io.write_enable := false.B

            // Read from both register files
            io.RegFileA_out := regFileA.io.out
            io.RegFileB_out := regFileB.io.out
        }
        is ("b0110111".U(7.W)) { // LUI x[rd] = sext(imm << 12)
            printf("Decoding LUI instruction\n")
            // Extract the fields
            val immm = io.instruction(31,12) // immediate value , 20 bits
            val rd = io.instruction(11,7) // destination register, 5 bits
            val imm_sext = Cat(immm, Fill(12, 0.U)) // 20 bits followed by 12 zeros to be 32 bit total

            printf("Extracted fields:\n")

            // Execute

            // Enable and write to the destination register
            regFileA.io.write_addr := rd // select destination register
            regFileB.io.write_addr := rd
            regFileA.io.write_enable := true.B // enable write in destination register
            regFileB.io.write_enable := true.B
            regFileA.io.in := imm_sext // Write to reg file A
            regFileB.io.in := imm_sext // Write to reg file B

            printf(p"LUI: rd = x$rd, imm_sext = ${Binary(imm_sext)}\n")

            // Registers will update on the next clock edge

            printf("LUI instruction executed\n")
        }
        is ("b0010011".U(7.W)) { // Immediate-type ALU operations
            printf("Decoding immediate-type ALU operation\n")
            // Extract the fields
            val rd = io.instruction(11,7) // destination register, 5 bits
            val funct3 = io.instruction(14,12) // funct3 field,
            val rs1 = io.instruction(19,15) // source register 1, 5 bits
            val imm = io.instruction(31,20) // immediate value, 12 bits

            printf("Extracted fields:\n")

            // addi (add immediate)
            when (funct3 === "b000".U(3.W)) { // x[rd] = x[rs1] + sext(imm)
                printf("Decoding ADDI instruction\n")
                // Add the 12-bit sign extended immediate to the value in rs1. put output in rd

                // Enable register file A to read rs1
                regFileA.io.write_enable := false.B
                regFileA.io.read_addr := rs1

                printf(p"ADDI: Reading rs1 = x$rs1\n")

                // Read rs1 (occurs in the same clock cycle because of combinational read)
                val rs1_value = regFileA.io.out

                // Compute result
                val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
                val result = rs1_value + imm_sext
                printf(p"ADDI: rs1_value = 0x${Binary(rs1_value)}, imm_sext = 0x${Binary(imm_sext)}, result = 0x${Binary(result)}\n")
                
                // Write result to rd
                regFileA.io.write_addr := rd // select destination register
                regFileB.io.write_addr := rd
                
                regFileA.io.write_enable := true.B // enable write in destination register
                regFileB.io.write_enable := true.B

                regFileA.io.in := result // Write to reg file A
                regFileB.io.in := result // Write to reg file B

                printf(p"ADDI: Writing result to rd = x$rd\n")

                // Registers will update on the next clock edge

                printf("ADDI instruction executed\n")
            }
        }
    }
}

/**
  * Object to generate Verilog/SystemVerilog for the module.
  * Customize firtoolOpts if needed.
  */
object Main extends App {
  ChiselStage.emitSystemVerilogFile(
    new Main(),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    )
  )
}

```


presentation compiler configuration:
Scala version: 2.13.16
Classpath:
<WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-HWUerHGvRVCZeTr56HnHqQ== [exists ], <HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar [exists ]
Options:
-language:reflectiveCalls -deprecation -feature -Xcheckinit -Ymacro-annotations -Yrangepos -Xplugin-require:semanticdb




#### Error stacktrace:

```
scala.collection.generic.CommonErrors$.indexOutOfBounds(CommonErrors.scala:23)
	scala.collection.mutable.ArrayBuffer.apply(ArrayBuffer.scala:102)
	scala.reflect.internal.Types$Type.findMemberInternal$1(Types.scala:1030)
	scala.reflect.internal.Types$Type.findMember(Types.scala:1035)
	scala.reflect.internal.Types$Type.memberBasedOnName(Types.scala:661)
	scala.reflect.internal.Types$Type.member(Types.scala:625)
	scala.tools.nsc.typechecker.Contexts$SymbolLookup.nextDefinition$1(Contexts.scala:1393)
	scala.tools.nsc.typechecker.Contexts$SymbolLookup.apply(Contexts.scala:1467)
	scala.tools.nsc.typechecker.Contexts$Context.lookupSymbol(Contexts.scala:1233)
	scala.tools.nsc.typechecker.Typers$Typer.typedIdent$2(Typers.scala:5731)
	scala.tools.nsc.typechecker.Typers$Typer.typedIdentOrWildcard$1(Typers.scala:5813)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6286)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedSelectOrSuperCall$1(Typers.scala:6442)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6289)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedSelectOrSuperCall$1(Typers.scala:6442)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6289)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedSelectOrSuperCall$1(Typers.scala:6442)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6289)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$41(Typers.scala:5262)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:703)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5264)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedStat$1(Typers.scala:6422)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typedStats$10(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedStats(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedTemplate(Typers.scala:2133)
	scala.tools.nsc.typechecker.Typers$Typer.typedClassDef(Typers.scala:1971)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6251)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedStat$1(Typers.scala:6422)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typedStats$10(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedStats(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedPackageDef$1(Typers.scala:5925)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6254)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Analyzer$typerFactory$TyperPhase.apply(Analyzer.scala:126)
	scala.tools.nsc.Global$GlobalPhase.applyPhase(Global.scala:483)
	scala.tools.nsc.interactive.Global$TyperRun.applyPhase(Global.scala:1370)
	scala.tools.nsc.interactive.Global$TyperRun.typeCheck(Global.scala:1363)
	scala.tools.nsc.interactive.Global.typeCheck(Global.scala:681)
	scala.meta.internal.pc.Compat.$anonfun$runOutline$1(Compat.scala:74)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:619)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:617)
	scala.collection.AbstractIterable.foreach(Iterable.scala:935)
	scala.meta.internal.pc.Compat.runOutline(Compat.scala:66)
	scala.meta.internal.pc.Compat.runOutline(Compat.scala:35)
	scala.meta.internal.pc.Compat.runOutline$(Compat.scala:33)
	scala.meta.internal.pc.MetalsGlobal.runOutline(MetalsGlobal.scala:39)
	scala.meta.internal.pc.ScalaCompilerWrapper.compiler(ScalaCompilerAccess.scala:18)
	scala.meta.internal.pc.ScalaCompilerWrapper.compiler(ScalaCompilerAccess.scala:13)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$semanticTokens$1(ScalaPresentationCompiler.scala:206)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:148)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withInterruptableCompiler$1(CompilerAccess.scala:92)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: -1 is out of bounds (min 0, max 2)
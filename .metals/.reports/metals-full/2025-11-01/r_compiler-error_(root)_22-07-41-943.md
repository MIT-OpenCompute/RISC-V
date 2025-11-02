error id: 14BE42B732F9664177CEB1C5432153B3
file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
### scala.reflect.internal.FatalError: 
  ThisType(value <local Main>) for sym which is not a class
     while compiling: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
        during phase: globalPhase=<no phase>, enteringPhase=parser
     library version: version 2.13.16
    compiler version: version 2.13.16
  reconstructed args: -deprecation -feature -Wconf:cat=feature:w -Wconf:cat=deprecation:ws -Wconf:cat=feature:ws -Wconf:cat=optimizer:ws -classpath <WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-HWUerHGvRVCZeTr56HnHqQ==:<HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar -language:reflectiveCalls -Xcheckinit -Xplugin-require:semanticdb -Yrangepos -Ymacro-expand:discard -Ymacro-annotations -Ycache-plugin-class-loader:last-modified -Ypresentation-any-thread

  last tree to typer: Select(Ident(chisel3), Binary)
       tree position: line 78 of file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
            tree tpe: chisel3.Binary.type
              symbol: object Binary in package chisel3
   symbol definition: object Binary (a ModuleSymbol)
      symbol package: chisel3
       symbol owners: object Binary
           call site: <none> in <none>

== Source file context for tree position ==

    75             regFileA.io.in := imm_sext // Write to reg file A
    76             regFileB.io.in := imm_sext // Write to reg file B
    77 
    78             printf(p"LUI: rd = x$rd, imm=${Binary(_CURSOR_im∂mm)}, imm_sext = ${Binary(imm_sext)}\n")
    79 
    80             // Registers will update on the next clock edge
    81         }

occurred in the presentation compiler.



action parameters:
offset: 3184
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
        val instruction = Input(UInt(32.W))
        val RegFileA_out = Output(UInt(32.W))
        val RegFileB_out = Output(UInt(32.W))
    })

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
            //printf(p"Register x$i in File A: ${Binary(regFileA.io.out)}, Register x$i in File B: ${Binary(regFileB.io.out)}\n")
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

            printf(p"LUI: rd = x$rd, imm=${Binary(@@im∂mm)}, imm_sext = ${Binary(imm_sext)}\n")

            // Registers will update on the next clock edge
        }
        is ("b0010011".U(7.W)) { // Immediate-type ALU operations
            printf("Decoding immediate-type ALU operation\n")
            // Extract the fields
            val rd = io.instruction(11,7) // destination register, 5 bits
            val funct3 = io.instruction(14,12) // funct3 field,
            val rs1 = io.instruction(19,15) // source register 1, 5 bits
            val imm = io.instruction(31,20) // immediate value, 12 bits

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

                //regFileA.io.read_addr := rd // set read address to rd to observe the written value (Required for the testbench to see the updated value)
                //regFileB.io.read_addr := rd
                // Registers will update on the next clock edge
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
scala.reflect.internal.Reporting.abort(Reporting.scala:70)
	scala.reflect.internal.Reporting.abort$(Reporting.scala:66)
	scala.reflect.internal.SymbolTable.abort(SymbolTable.scala:28)
	scala.reflect.internal.Types$ThisType.<init>(Types.scala:1394)
	scala.reflect.internal.Types$UniqueThisType.<init>(Types.scala:1414)
	scala.reflect.internal.Types$ThisType$.apply(Types.scala:1418)
	scala.meta.internal.pc.AutoImportsProvider$$anonfun$1.applyOrElse(AutoImportsProvider.scala:97)
	scala.meta.internal.pc.AutoImportsProvider$$anonfun$1.applyOrElse(AutoImportsProvider.scala:79)
	scala.collection.immutable.List.collect(List.scala:268)
	scala.meta.internal.pc.AutoImportsProvider.autoImports(AutoImportsProvider.scala:79)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$autoImports$1(ScalaPresentationCompiler.scala:399)
	scala.meta.internal.pc.CompilerAccess.retryWithCleanCompiler(CompilerAccess.scala:182)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withSharedCompiler$1(CompilerAccess.scala:155)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:154)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withInterruptableCompiler$1(CompilerAccess.scala:92)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

scala.reflect.internal.FatalError: 
  ThisType(value <local Main>) for sym which is not a class
     while compiling: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
        during phase: globalPhase=<no phase>, enteringPhase=parser
     library version: version 2.13.16
    compiler version: version 2.13.16
  reconstructed args: -deprecation -feature -Wconf:cat=feature:w -Wconf:cat=deprecation:ws -Wconf:cat=feature:ws -Wconf:cat=optimizer:ws -classpath <WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-HWUerHGvRVCZeTr56HnHqQ==:<HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar:<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar -language:reflectiveCalls -Xcheckinit -Xplugin-require:semanticdb -Yrangepos -Ymacro-expand:discard -Ymacro-annotations -Ycache-plugin-class-loader:last-modified -Ypresentation-any-thread

  last tree to typer: Select(Ident(chisel3), Binary)
       tree position: line 78 of file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
            tree tpe: chisel3.Binary.type
              symbol: object Binary in package chisel3
   symbol definition: object Binary (a ModuleSymbol)
      symbol package: chisel3
       symbol owners: object Binary
           call site: <none> in <none>

== Source file context for tree position ==

    75             regFileA.io.in := imm_sext // Write to reg file A
    76             regFileB.io.in := imm_sext // Write to reg file B
    77 
    78             printf(p"LUI: rd = x$rd, imm=${Binary(_CURSOR_im∂mm)}, imm_sext = ${Binary(imm_sext)}\n")
    79 
    80             // Registers will update on the next clock edge
    81         }
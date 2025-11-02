error id: BD5BC9BABB2E9A06724309727A5668FC
file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
### scala.ScalaReflectionException: value imm_sext is not a method

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

/**
  * A simple register file module with parameterizable width and number of registers.
  * 
  * TODO: Set up two simultaneous read ports
  */

class Main() extends Module {
    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
        val regFileA_out = Output(UInt(32.W))
        val regFileB_out = Output(UInt(32.W))
    })

    // set up register files A and B. They will have identical contents so that we can do dual reads.
    val regFileA = Module(new Registers())
    val regFileB = Module(new Registers())

    // Provide safe/default connections for all register-file inputs so
    // there are no uninitialized sinks when a particular opcode path
    // doesn't drive them. We assign defaults first and override in
    // the opcode-specific when blocks below.
    regFileA.io.enable := false.B
    regFileA.io.write_enable := false.B
    regFileA.io.select := 0.U
    regFileA.io.in := 0.U

    regFileB.io.enable := false.B
    regFileB.io.write_enable := false.B
    regFileB.io.select := 0.U
    regFileB.io.in := 0.U
    
    io.regFileA_out := regFileA.io.out
    io.regFileB_out := regFileB.io.out

     // opcode
    val opcode = io.instruction(6,0) // 7 bits
    
    when (opcode === "b0110111".U(7.W)) { // lui instruction, load upper sign extended immediate into register rd
        val rd = io.instruction(11,7) // destination register, 5 bits
        val imm = io.instruction(31,12) // immediate value , 20 bits

        // Sign extend the 20 bit immediate to 32 bits by appending 12 zeros at the end
        val imm_sext = Cat(imm, Fill(12, 0.U))/ // 20 bits followed by 12 zeros to be 32 bit total
        
        // Move imm_sext into register rd
        regFileA.io.select := rd // select destination register
        regFileB.io.select := rd
        regFileA.io.enable := true.B // enable destination register
        regFileB.io.enable := true.B
        regFileA.io.write_enable := true.B // enable write in destination register
        regFileB.io.write_enable := true.B
        regFileA.io.in := imm_sext // Write to reg file A
        regFileB.io.in := imm_sext // Write to reg file B
    } 

    when (opcode === "b0010011".U(7.W)) { // immediate-type ALU operations
        val rd = io.instruction(11,7) // destination register, 5 bits
        val funct3 = io.instruction(14,12) // funct3 field, 3 bits
        val rs1 = io.instruction(19,15) // source register 1, 5 bits
        val imm = io.instruction(31,20) // immediate value, 12 bits

        // addi (add immediate)
        when (funct3 === "b000".U(3.W)) {
            // Add the 12-bit sign extended immediate to the value in rs1. put output in rd
            regFileA.io.enable := true.B
            regFileA.io.write_enable := false.B
            regFileA.io.select := rs1
            // Read rs1
            val rs1_value = regFileA.io.out
            // Compute result
            val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
            val result = rs1_value + imm_sext

            // Write result to rd
            regFileA.io.select := rd // select destination register
            regFileB.io.select := rd
            
            regFileA.io.enable := true.B // enable destination register
            regFileB.io.enable := true.B
            
            regFileA.io.write_enable := true.B // enable write in destination register
            regFileB.io.write_enable := true.B

            regFileA.io.in := result // Write to reg file A
            regFileB.io.in := result // Write to reg file B
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
scala.reflect.api.Symbols$SymbolApi.asMethod(Symbols.scala:240)
	scala.reflect.api.Symbols$SymbolApi.asMethod$(Symbols.scala:234)
	scala.reflect.internal.Symbols$SymbolContextApiImpl.asMethod(Symbols.scala:101)
	scala.tools.nsc.typechecker.ContextErrors$TyperContextErrors$TyperErrorGen$.MissingArgsForMethodTpeError(ContextErrors.scala:818)
	scala.tools.nsc.typechecker.Typers$Typer.adaptMethodTypeToExpr$1(Typers.scala:989)
	scala.tools.nsc.typechecker.Typers$Typer.adapt(Typers.scala:1346)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6359)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgWithFormal$1(PatternTypers.scala:134)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.$anonfun$typedArgsForFormals$4(PatternTypers.scala:150)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgsForFormals(PatternTypers.scala:150)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgsForFormals$(PatternTypers.scala:131)
	scala.tools.nsc.typechecker.Typers$Typer.typedArgsForFormals(Typers.scala:202)
	scala.tools.nsc.typechecker.Typers$Typer.handleMonomorphicCall$1(Typers.scala:3921)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:3972)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$28(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:703)
	scala.tools.nsc.typechecker.Typers$Typer.tryTypedApply$1(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5283)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedStat$1(Typers.scala:6422)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typedStats$10(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedStats(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedBlock(Typers.scala:2643)
	scala.tools.nsc.typechecker.Typers$Typer.typedOutsidePatternMode$1(Typers.scala:6262)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6298)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgWithFormal$1(PatternTypers.scala:134)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.$anonfun$typedArgsForFormals$4(PatternTypers.scala:150)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgsForFormals(PatternTypers.scala:150)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgsForFormals$(PatternTypers.scala:131)
	scala.tools.nsc.typechecker.Typers$Typer.typedArgsForFormals(Typers.scala:202)
	scala.tools.nsc.typechecker.Typers$Typer.handleMonomorphicCall$1(Typers.scala:3921)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:3972)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5285)
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

scala.ScalaReflectionException: value imm_sext is not a method
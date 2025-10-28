error id: 56A620A7965F1DA4C78131BA201247A6
file://<WORKSPACE>/src/main/scala/RISC-V/registers.scala
### java.lang.IndexOutOfBoundsException: -1 is out of bounds (min 0, max 2)

occurred in the presentation compiler.



action parameters:
uri: file://<WORKSPACE>/src/main/scala/RISC-V/registers.scala
text:
```scala
// See README.md for license details.

package mymodule

import chisel3._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage

/**
  * Template for a Chisel module.
  * Replace internal logic and IO with your own design.
  */
class registers(val width: Int = 32, val nreg: Int = 32) extends Module {
    // Define input/output interface
    val io = IO(new Bundle {
        val in  = Input(UInt(width.W))
        val out = Output(UInt(width.W))
        val select = Input(UInt(nreg.W))
        val rw = Input(Bool())
        val enable = Inpiut(Bool())
    })

    // Internal register array
    val regs = RegInit(VecInit(Seq.fill(nreg)(0.U(width.W))))

    when(enable) {
        when(rw) {
            regs(select) := io.in
        }
        when(Q)
        regs(select) :=
    }
}

/**
  * Object to generate Verilog/SystemVerilog for the module.
  * Customize firtoolOpts if needed.
  */
object MyModule extends App {
  ChiselStage.emitSystemVerilogFile(
    new MyModule(),
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
<WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-sNZYM_jgQqGYKf4HT75EYg== [exists ], <HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar [exists ]
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
	scala.tools.nsc.typechecker.Typers$Typer.typedIdent$2(Typers.scala:5719)
	scala.tools.nsc.typechecker.Typers$Typer.typedIdentOrWildcard$1(Typers.scala:5813)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6286)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$41(Typers.scala:5262)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:703)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5264)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.typedImplicit1(Implicits.scala:869)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.typedImplicit0(Implicits.scala:824)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.scala$tools$nsc$typechecker$Implicits$ImplicitSearch$$typedImplicit(Implicits.scala:639)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch$ImplicitComputation.rankImplicits(Implicits.scala:1219)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch$ImplicitComputation.findBest(Implicits.scala:1260)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.searchImplicit(Implicits.scala:1319)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.bestImplicit(Implicits.scala:1716)
	scala.tools.nsc.typechecker.Implicits.inferImplicit1(Implicits.scala:112)
	scala.tools.nsc.typechecker.Implicits.inferImplicit(Implicits.scala:91)
	scala.tools.nsc.typechecker.Implicits.inferImplicit$(Implicits.scala:88)
	scala.meta.internal.pc.MetalsGlobal$MetalsInteractiveAnalyzer.inferImplicit(MetalsGlobal.scala:85)
	scala.tools.nsc.typechecker.Implicits.inferImplicitView(Implicits.scala:50)
	scala.tools.nsc.typechecker.Implicits.inferImplicitView$(Implicits.scala:49)
	scala.meta.internal.pc.MetalsGlobal$MetalsInteractiveAnalyzer.inferImplicitView(MetalsGlobal.scala:85)
	scala.tools.nsc.typechecker.Typers$Typer.inferView(Typers.scala:332)
	scala.tools.nsc.typechecker.Typers$Typer.adaptToMember(Typers.scala:1409)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$adaptToMemberWithArgs$6(Typers.scala:1458)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:717)
	scala.tools.nsc.typechecker.Typers$Typer.adaptToMemberWithArgs(Typers.scala:1458)
	scala.tools.nsc.typechecker.Typers$Typer.typedSelect$1(Typers.scala:5505)
	scala.tools.nsc.typechecker.Typers$Typer.typedSelectOrSuperCall$1(Typers.scala:5660)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6289)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg0$1(Typers.scala:3675)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$doTypedApply$7(Typers.scala:3690)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$doTypedApply$6(Typers.scala:3673)
	scala.tools.nsc.typechecker.Contexts$Context.savingUndeterminedTypeParams(Contexts.scala:497)
	scala.tools.nsc.typechecker.Typers$Typer.handleOverloaded$1(Typers.scala:3670)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:3732)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$28(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:717)
	scala.tools.nsc.typechecker.Typers$Typer.tryTypedApply$1(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5283)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.Typers$Typer.handlePolymorphicCall$1(Typers.scala:3990)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:4009)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5285)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg0$1(Typers.scala:3675)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$doTypedApply$7(Typers.scala:3690)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$doTypedApply$6(Typers.scala:3673)
	scala.tools.nsc.typechecker.Contexts$Context.savingUndeterminedTypeParams(Contexts.scala:497)
	scala.tools.nsc.typechecker.Typers$Typer.handleOverloaded$1(Typers.scala:3670)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:3732)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$28(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:717)
	scala.tools.nsc.typechecker.Typers$Typer.tryTypedApply$1(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5283)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.Typers$Typer.handlePolymorphicCall$1(Typers.scala:3990)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedApply(Typers.scala:4009)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$28(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.silent(Typers.scala:703)
	scala.tools.nsc.typechecker.Typers$Typer.tryTypedApply$1(Typers.scala:5195)
	scala.tools.nsc.typechecker.Typers$Typer.normalTypedApply$1(Typers.scala:5283)
	scala.tools.nsc.typechecker.Typers$Typer.typedApply$1(Typers.scala:5296)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6288)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.computeType(Typers.scala:6433)
	scala.tools.nsc.typechecker.Namers$Namer.assignTypeToTree(Namers.scala:1085)
	scala.tools.nsc.typechecker.Namers$Namer.inferredValTpt$1(Namers.scala:1732)
	scala.tools.nsc.typechecker.Namers$Namer.valDefSig(Namers.scala:1745)
	scala.tools.nsc.typechecker.Namers$Namer.memberSig(Namers.scala:1930)
	scala.tools.nsc.typechecker.Namers$Namer.typeSig(Namers.scala:1880)
	scala.tools.nsc.typechecker.Namers$Namer$ValTypeCompleter.completeImpl(Namers.scala:898)
	scala.tools.nsc.typechecker.Namers$LockingTypeCompleter.complete(Namers.scala:2077)
	scala.tools.nsc.typechecker.Namers$LockingTypeCompleter.complete$(Namers.scala:2075)
	scala.tools.nsc.typechecker.Namers$TypeCompleterBase.complete(Namers.scala:2070)
	scala.reflect.internal.Symbols$Symbol.completeInfo(Symbols.scala:1583)
	scala.reflect.internal.Symbols$Symbol.info(Symbols.scala:1548)
	scala.reflect.internal.Symbols$Symbol.initialize(Symbols.scala:1747)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:5916)
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
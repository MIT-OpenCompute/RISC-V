error id: 705DE9E34A874B927A01841A85C879E1
file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala
### java.lang.IndexOutOfBoundsException: -1 is out of bounds (min 0, max 2)

occurred in the presentation compiler.



action parameters:
uri: file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala
text:
```scala
// See README.md for license details.

package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "main should pass tests" in {
    simulate(new Main()) { dut =>
        // Test LUI instruction: lui x1, 
        val lui_instruction = "b10000000000000000000000010110111".U(32.W) // opcode for LUI with rd = x1 and imm = 0x123

        // Attempt loading LUI instruction
        dut.io.instruction.poke(lui_instruction) // Load the immediate into register x1
        
        dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write

        // Check that register x1 in both register files has the correct value
        dut.io.regFileA_out.expect("b10000000000000000000000000000000".U(32.W))
        dut.io.regFileB_out.expect("b10000000000000000000000000000000".U(32.W))

        dut.clock.step(1) // Step another clock to ensure stable state before next instruction
fu
        // Test ADDI instruction: addi x2, x1, b111111111111
        val addi_instruction = "b11111111111100001000000100010011".U(32.W) // opcode for ADDI with rd = x2, rs1 = x1, imm = -1
        dut.io.instruction.poke(addi_instruction) // Load the ADDI instruction
        dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write
        // Check that register x2 in register file A has the correct value (x1 + (-1))
        dut.io.regFileA_out.expect("b10000000000000000000111111111111".U(32.W))
        dut.io.regFileB_out.expect("b10000000000000000000111111111111".U(32.W))
    }
  }
}
```


presentation compiler configuration:
Scala version: 2.13.16
Classpath:
<WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/test-classes-Metals-HWUerHGvRVCZeTr56HnHqQ== [exists ], <HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar [exists ], <WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-HWUerHGvRVCZeTr56HnHqQ== [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest_2.13/3.2.19/scalatest_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-core_2.13/3.2.19/scalatest-core_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-featurespec_2.13/3.2.19/scalatest-featurespec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-flatspec_2.13/3.2.19/scalatest-flatspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-freespec_2.13/3.2.19/scalatest-freespec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-funsuite_2.13/3.2.19/scalatest-funsuite_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-funspec_2.13/3.2.19/scalatest-funspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-propspec_2.13/3.2.19/scalatest-propspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-refspec_2.13/3.2.19/scalatest-refspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-wordspec_2.13/3.2.19/scalatest-wordspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-diagrams_2.13/3.2.19/scalatest-diagrams_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-matchers-core_2.13/3.2.19/scalatest-matchers-core_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-shouldmatchers_2.13/3.2.19/scalatest-shouldmatchers_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-mustmatchers_2.13/3.2.19/scalatest-mustmatchers_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-compatible/3.2.19/scalatest-compatible-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalactic/scalactic_2.13/3.2.19/scalactic_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar [exists ]
Options:
-language:reflectiveCalls -deprecation -feature -Xcheckinit -Ymacro-annotations -Yrangepos -Xplugin-require:semanticdb




#### Error stacktrace:

```
scala.collection.generic.CommonErrors$.indexOutOfBounds(CommonErrors.scala:23)
	scala.collection.mutable.ArrayBuffer.apply(ArrayBuffer.scala:102)
	scala.reflect.internal.Types$Type.findMemberInternal$1(Types.scala:1030)
	scala.reflect.internal.Types$Type.findMember(Types.scala:1035)
	scala.reflect.internal.Types$Type.memberBasedOnName(Types.scala:661)
	scala.reflect.internal.Types$Type.nonPrivateMember(Types.scala:632)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.$anonfun$checkCompatibility$1(Implicits.scala:774)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.$anonfun$checkCompatibility$1$adapted(Implicits.scala:774)
	scala.collection.IterableOnceOps.exists(IterableOnce.scala:647)
	scala.collection.IterableOnceOps.exists$(IterableOnce.scala:644)
	scala.collection.AbstractIterator.exists(Iterator.scala:1306)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.loop$3(Implicits.scala:774)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.checkCompatibility(Implicits.scala:780)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.normSubType(Implicits.scala:480)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.matchesPt(Implicits.scala:664)
	scala.tools.nsc.typechecker.Implicits$ImplicitSearch.typedImplicit1(Implicits.scala:908)
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
	scala.tools.nsc.typechecker.Typers$Typer.typedStat$1(Typers.scala:6422)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typedStats$10(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedStats(Typers.scala:3547)
	scala.tools.nsc.typechecker.Typers$Typer.typedBlock(Typers.scala:2643)
	scala.tools.nsc.typechecker.Typers$Typer.typedOutsidePatternMode$1(Typers.scala:6262)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6298)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.doTypedFunction(Typers.scala:6433)
	scala.tools.nsc.typechecker.Typers$Typer.typedFunction(Typers.scala:3203)
	scala.tools.nsc.typechecker.Typers$Typer.$anonfun$typed1$121(Typers.scala:6226)
	scala.tools.nsc.typechecker.Typers$Typer.typedFunction$1(Typers.scala:512)
	scala.tools.nsc.typechecker.Typers$Typer.typedOutsidePatternMode$1(Typers.scala:6266)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6298)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6344)
	scala.tools.nsc.typechecker.Typers$Typer.typedArg(Typers.scala:3565)
	scala.tools.nsc.typechecker.PatternTypers$PatternTyper.typedArgWithFormal$1(PatternTypers.scala:136)
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
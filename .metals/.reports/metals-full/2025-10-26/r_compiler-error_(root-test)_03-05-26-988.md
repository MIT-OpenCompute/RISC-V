error id: 5E1338CE930ABD0D82AD99EBCEFD6D62
file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala
### scala.reflect.internal.FatalError: no context found for source-file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala,line-1,offset=31

occurred in the presentation compiler.



action parameters:
offset: 31
uri: file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala
text:
```scala
// See README.md for license de@@tails.

package `RISC-V`

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
  * Boilerplate test specification for a Chisel module.
  * Replace MyModule with your module and add proper test cases.
  */
class RegistersSpec extends AnyFlatSpec with ChiselScalatestTester {

  "registers" should "pass basic tests" in {
    // Instantiate your module
    test(new registers()) { c =>
      
      // Example: poke inputs
      c.io.in1.poke(10.U)
      c.io.in2.poke(5.U)
      c.io.load.poke(true.B)
      
      // Step one clock cycle
      c.clock.step(1)
      
      // Example: check output
      c.io.out.expect(10.U)  // Replace with expected value
      c.io.valid.expect(false.B) // Replace with expected value
      
      // Advance clock if needed
      c.clock.step(5)
      
      // More checks here
      // c.io.out.expect(...)
    }
  }
}

```


presentation compiler configuration:
Scala version: 2.13.16
Classpath:
<WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/test-classes-Metals-sNZYM_jgQqGYKf4HT75EYg== [exists ], <HOME>/Library/Caches/bloop/semanticdb/com.sourcegraph.semanticdb-javac.0.11.1/semanticdb-javac-0.11.1.jar [exists ], <WORKSPACE>/.bloop/root/bloop-bsp-clients-classes/classes-Metals-sNZYM_jgQqGYKf4HT75EYg== [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/chisel_2.13/7.0.0/chisel_2.13-7.0.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest_2.13/3.2.19/scalatest_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/github/scopt/scopt_2.13/4.1.0/scopt_2.13-4.1.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-text/1.13.1/commons-text-1.13.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_2.13/0.10.7/os-lib_2.13-0.10.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native_2.13/4.0.7/json4s-native_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/data-class_2.13/0.2.7/data-class_2.13-0.2.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.16/scala-reflect-2.13.16.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_2.13/3.3.1/upickle_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/chipsalliance/firtool-resolver_2.13/2.0.1/firtool-resolver_2.13-2.0.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-core_2.13/3.2.19/scalatest-core_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-featurespec_2.13/3.2.19/scalatest-featurespec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-flatspec_2.13/3.2.19/scalatest-flatspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-freespec_2.13/3.2.19/scalatest-freespec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-funsuite_2.13/3.2.19/scalatest-funsuite_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-funspec_2.13/3.2.19/scalatest-funspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-propspec_2.13/3.2.19/scalatest-propspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-refspec_2.13/3.2.19/scalatest-refspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-wordspec_2.13/3.2.19/scalatest-wordspec_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-diagrams_2.13/3.2.19/scalatest-diagrams_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-matchers-core_2.13/3.2.19/scalatest-matchers-core_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-shouldmatchers_2.13/3.2.19/scalatest-shouldmatchers_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-mustmatchers_2.13/3.2.19/scalatest-mustmatchers_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_2.13/1.1.1/geny_2.13-1.1.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-core_2.13/4.0.7/json4s-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-native-core_2.13/4.0.7/json4s-native-core_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_2.13/3.3.1/ujson_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_2.13/3.3.1/upack_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_2.13/3.3.1/upickle-implicits_2.13-3.3.1.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.13/2.2.0/scala-xml_2.13-2.2.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.11.0/scala-collection-compat_2.13-2.11.0.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalatest/scalatest-compatible/3.2.19/scalatest-compatible-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scalactic/scalactic_2.13/3.2.19/scalactic_2.13-3.2.19.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-ast_2.13/4.0.7/json4s-ast_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/json4s/json4s-scalap_2.13/4.0.7/json4s-scalap_2.13-4.0.7.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar [exists ], <HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_2.13/3.3.1/upickle-core_2.13-3.3.1.jar [exists ]
Options:
-language:reflectiveCalls -deprecation -feature -Xcheckinit -Ymacro-annotations -Yrangepos -Xplugin-require:semanticdb




#### Error stacktrace:

```
scala.tools.nsc.interactive.CompilerControl.$anonfun$doLocateContext$1(CompilerControl.scala:100)
	scala.tools.nsc.interactive.CompilerControl.doLocateContext(CompilerControl.scala:100)
	scala.tools.nsc.interactive.CompilerControl.doLocateContext$(CompilerControl.scala:99)
	scala.tools.nsc.interactive.Global.doLocateContext(Global.scala:115)
	scala.meta.internal.pc.PcDefinitionProvider.definitionTypedTreeAt(PcDefinitionProvider.scala:181)
	scala.meta.internal.pc.PcDefinitionProvider.definition(PcDefinitionProvider.scala:69)
	scala.meta.internal.pc.PcDefinitionProvider.definition(PcDefinitionProvider.scala:17)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$definition$1(ScalaPresentationCompiler.scala:490)
	scala.meta.internal.pc.CompilerAccess.retryWithCleanCompiler(CompilerAccess.scala:182)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withSharedCompiler$1(CompilerAccess.scala:155)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:154)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withNonInterruptableCompiler$1(CompilerAccess.scala:132)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

scala.reflect.internal.FatalError: no context found for source-file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala,line-1,offset=31
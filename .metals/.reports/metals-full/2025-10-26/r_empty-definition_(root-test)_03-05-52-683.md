error id: file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala:
file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chisel3/chiseltest.
	 -chiseltest/chiseltest.
	 -chiseltest.
	 -scala/Predef.chiseltest.
offset: 127
uri: file://<WORKSPACE>/src/test/scala/RISC-V/RegistersSpec.scala
text:
```scala
// See README.md for license details.

package `RISC-V`
import chisel3._
import chisel3.experimental.BundleLiterals._
import ch@@isel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

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


#### Short summary: 

empty definition using pc, found symbol in pc: 
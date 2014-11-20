package date

import org.junit.Assert
import common.CommonTest
import org.junit.runner.RunWith
import date.api.SkillFile
import date.api.Read
import date.internal.SkillException

/**
 * Tests the file reading capabilities.
 */
class ParseTest extends CommonTest {

  def read(s: String) = SkillFile.open("src/test/resources/"+s, Read)

  test("two dates") {
    val σ = read("date-example.sf")

    val it = σ.Date.all;
    Assert.assertEquals(1L, it.next.date)
    Assert.assertEquals(-1L, it.next.date)
    assert(!it.hasNext, "there shouldn't be any more elemnets!")
  }

  test("simple nodes") { Assert.assertNotNull(read("node.sf")) }
  test("simple test") { Assert.assertNotNull(read("date-example.sf")) }
  /**
   * @see § 6.2.3.Fig.3
   */
  test("two node blocks") { Assert.assertNotNull(read("twoNodeBlocks.sf")) }
  /**
   * @see § 6.2.3.Fig.4
   */
  test("colored nodes") { Assert.assertNotNull(read("coloredNodes.sf")) }
  test("four colored nodes") { Assert.assertNotNull(read("fourColoredNodes.sf")) }
  test("empty blocks") { Assert.assertNotNull(read("emptyBlocks.sf")) }
  test("two types") { Assert.assertNotNull(read("twoTypes.sf")) }
  test("trivial type definition") { Assert.assertNotNull(read("trivialType.sf")) }
  
  test("data chunk is too long") {
    intercept[SkillException] {
      read("illformed/longerDataChunk.sf").Date.all
    }
  }
  test("data chunk is too short") {
    intercept[SkillException] {
      read("illformed/shorterDataChunk.sf").Date.all
    }
  }
  test("incompatible field types") {
    intercept[SkillException] {
      read("illformed/incompatibleType.sf").Date.all
    }
  }
  test("reserved type ID") {
    intercept[SkillException] {
      read("illformed/illegalTypeID.sf").Date.all
    }
  }
  test("missing user type") {
    intercept[SkillException] {
      read("illformed/missingUserType.sf").Date.all
    }
  }
  test("illegal string pool offset") {
    intercept[SkillException] {
      read("illformed/illegalStringPoolOffsets.sf").Date.all
    }
  }
  test("duplicate type definition in the first block") {
    intercept[SkillException] {
      read("illformed/duplicateDefinition.sf").Date.all
    }
  }
  test("append in the first block") {
    intercept[SkillException] {
      read("illformed/duplicateDefinitionMixed.sf").Date.all
    }
  }
  test("duplicate append in the same block") {
    intercept[SkillException] {
      read("illformed/duplicateDefinitionSecondBlock.sf").Date.all
    }
  }
}
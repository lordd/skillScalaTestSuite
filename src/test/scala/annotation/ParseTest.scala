package annotation

import org.junit.Assert

import annotation.api.SkillState
import annotation.internal.AnnotationTypeCastException
import annotation.internal.ParseException
import annotation.internal.PoolSizeMissmatchError
import annotation.internal.TypeMissmatchError
import annotation.internal.UnexpectedEOF
import common.CommonTest

/**
 * Tests the file reading capabilities.
 */
class ParseTest extends CommonTest {

  test("two dates") {
    val σ = SkillState.read("date-example.sf")

    val it = σ.Date.all;
    Assert.assertEquals(1L, it.next.date)
    Assert.assertEquals(-1L, it.next.date)
    assert(!it.hasNext, "there shouldn't be any more elemnets!")
  }

  test("simple nodes") { Assert.assertNotNull(SkillState.read("node.sf")) }
  test("simple test") { Assert.assertNotNull(SkillState.read("date-example.sf")) }
  /**
   * @see § 6.2.3.Fig.3
   */
  test("two node blocks") { Assert.assertNotNull(SkillState.read("twoNodeBlocks.sf")) }
  /**
   * @see § 6.2.3.Fig.4
   */
  test("colored nodes") { Assert.assertNotNull(SkillState.read("coloredNodes.sf")) }
  test("four colored nodes") { Assert.assertNotNull(SkillState.read("fourColoredNodes.sf")) }
  test("empty blocks") { Assert.assertNotNull(SkillState.read("emptyBlocks.sf")) }
  test("two types") { Assert.assertNotNull(SkillState.read("twoTypes.sf")) }
  test("trivial type definition") { Assert.assertNotNull(SkillState.read("trivialType.sf")) }

  /**
   * a regression test checking correct treatment of types without fields
   */
  test("append without fields")(SkillState.read("noFieldRegressionTest.sf").Date.all)

  /**
   * a regression test checking correct treatment of types without fields
   */
  test("appended dates without fields")(assert(SkillState.read("noFieldRegressionDates.sf").Date.all.size === 2))

  /**
   * null pointers are legal in regular fields if restricted to be nullable
   *  (although the behavior is not visible here due to lazyness)
   */
  test("nullable restricted null pointer") { SkillState.read("nullableNode.sf").Test.all }
  /**
   * null pointers are legal in annotations
   */
  test("null pointer in an annotation") { SkillState.read("nullAnnotation.sf").Test.all }

  /**
   * null pointers are not legal in regular fields
   *
   * @note this is the lazy case, i.e. the node pointer is never evaluated
   */
  test("null pointer in a nonnull field; lazy case!") {
    SkillState.read("illformed/nullNode.sf").Test.all
  }

  test("data chunk is too long") {
    val thrown = intercept[PoolSizeMissmatchError] {
      SkillState.read("illformed/longerDataChunk.sf").Date.all
    }
    assert(thrown.getMessage === "expected: 2, was: 3, field type: v64")
  }
  test("data chunk is too short") {
    val thrown = intercept[PoolSizeMissmatchError] {
      SkillState.read("illformed/shorterDataChunk.sf").Date.all
    }
    assert(thrown.getMessage === "expected: 2, was: 1, field type: v64")
  }
  test("incompatible field types") {
    val thrown = intercept[TypeMissmatchError] {
      SkillState.read("illformed/incompatibleType.sf").Date.all
    }
    assert(thrown.getMessage === """During construction of DateStoragePool.date: Encountered incompatible type "date" (expected: v64)""")
  }
  test("reserved type ID") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/illegalTypeID.sf").Date.all
    }
    assert(thrown.getMessage === """In block 1 @17: Invalid type ID: 16""")
  }
  test("missing user type") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/missingUserType.sf").Date.all
    }
    assert(thrown.getMessage === "In block 1 @19: date.date refers to inexistent user type 1 (user types: 0 -> date)")
  }
  test("illegal string pool offset") {
    val thrown = intercept[UnexpectedEOF] {
      SkillState.read("illformed/illegalStringPoolOffsets.sf").Date.all
    }
    assert(thrown.getMessage === "@5 while dropping 353 bytes")
  }
  test("missing field declarations in second block") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/missingFieldInSecondBlock.sf").Date.all
    }
    assert(thrown.getMessage === "In block 2 @22: Type a has 0 fields (requires 1)")
  }

  test("duplicate type definition in the first block") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/duplicateDefinition.sf").Date.all
    }
    assert(thrown.getMessage === "In block 1 @13: Duplicate definition of type a")
  }
  test("append in the first block") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/duplicateDefinitionMixed.sf").Date.all
    }
    assert(thrown.getMessage === "In block 1 @13: Duplicate definition of type a")
  }
  test("duplicate append in the same block") {
    val thrown = intercept[ParseException] {
      SkillState.read("illformed/duplicateDefinitionSecondBlock.sf").Date.all
    }
    assert(thrown.getMessage === "In block 2 @18: Duplicate definition of type a")
  }

  // annotation related tests
  test("read annotation") { SkillState.read("annotationTest.sf") }

  test("check annotation") {
    val state = SkillState.read("annotationTest.sf")
    val t = state.Test.all.next
    val d = state.Date.all.next
    assert(t.f === d)
  }

  test("change annotation field") {
    val σ = SkillState.read("annotationTest.sf")
    val t = σ.Test.all.next
    val d = σ.Date.all.next
    t.f = t
    assert(t.f === t)
  }

  test("annotation type-safety") {
    intercept[AnnotationTypeCastException] {
      val σ = SkillState.read("annotationTest.sf")
      val t = σ.Test.all.next
      val d = σ.Date.all.next
      // no its not
      if (t.f == t)
        fail;
    }
  }
}
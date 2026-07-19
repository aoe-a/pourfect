package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrinderCatalogTest {

    @Test
    fun `catalog covers the requested brands`() {
        val brands = GrinderCatalog.all.map { it.brand }.toSet()
        for (brand in listOf("Comandante", "1Zpresso", "Timemore", "Kingrinder")) {
            assertTrue("missing brand $brand", brand in brands)
        }
    }

    @Test
    fun `the users timemore c5s pro is in the catalog`() {
        assertNotNull(GrinderCatalog.all.firstOrNull { it.model.contains("C5S Pro") })
    }

    @Test
    fun `every grinder has a V60 setting and non-blank texts`() {
        for (grinder in GrinderCatalog.all) {
            assertTrue("${grinder.model} missing V60", GrindMethod.V60 in grinder.settings)
            for ((method, text) in grinder.settings) {
                assertTrue("${grinder.model} $method blank", text.isNotBlank())
            }
            assertTrue(grinder.id.isNotBlank() && grinder.model.isNotBlank())
        }
    }

    @Test
    fun `ids are unique and lookup works`() {
        val ids = GrinderCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        val first = GrinderCatalog.all.first()
        assertEquals(first, GrinderCatalog.byId(first.id))
    }

    @Test
    fun `unknown id returns null`() {
        assertEquals(null, GrinderCatalog.byId("not-a-grinder"))
    }
}

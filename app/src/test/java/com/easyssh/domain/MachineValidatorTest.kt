package com.easyssh.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineValidatorTest {
    @Test
    fun staticDraftRequiresHostAndKey() {
        val errors = MachineValidator.validateDraft(
            draft = MachineDraft(
                alias = "AWS",
                username = "ec2-user",
                host = "",
                port = "22",
                ipMode = IpMode.STATIC
            ),
            hasSelectedKey = false
        )

        assertTrue(errors.any { it.contains("IP") })
        assertTrue(errors.any { it.contains("chave") })
    }

    @Test
    fun rotatingDraftDoesNotRequireStaticHost() {
        val errors = MachineValidator.validateDraft(
            draft = MachineDraft(
                alias = "AWS",
                username = "ec2-user",
                host = "",
                port = "22",
                ipMode = IpMode.ROTATING
            ),
            hasSelectedKey = true
        )

        assertTrue(errors.isEmpty())
    }

    @Test
    fun hostRejectsSchemesAndWhitespace() {
        assertFalse(MachineValidator.isValidHost("ssh://1.2.3.4"))
        assertFalse(MachineValidator.isValidHost("1.2.3.4 test"))
        assertTrue(MachineValidator.isValidHost("ec2-1-2-3-4.compute.amazonaws.com"))
        assertTrue(MachineValidator.isValidHost("1.2.3.4"))
    }
}


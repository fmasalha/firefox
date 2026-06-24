/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.llm.ext

import kotlinx.coroutines.test.runTest
import mozilla.components.concept.llm.AttestationFailure
import mozilla.components.concept.llm.AuthFailure
import mozilla.components.concept.llm.Llm
import mozilla.components.lib.llm.mlpa.MlpaTokenProvider
import mozilla.components.lib.llm.mlpa.service.AuthorizationToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAttestationFailure : Llm.Exception("attestation failed"), AttestationFailure

class MlpaTokenProviderTest {

    @Test
    fun `GIVEN the first provider succeeds WHEN fetching THEN its token is returned`() = runTest {
        val expected = AuthorizationToken.Integrity("first")
        val provider = MlpaTokenProvider.choose(
            { Result.success(expected) },
            { Result.failure(FakeAttestationFailure()) },
        )

        assertEquals(expected, provider.fetchToken().getOrThrow())
    }

    @Test
    fun `GIVEN the first provider fails WHEN fetching THEN the next provider's token is returned`() = runTest {
        val expected = AuthorizationToken.Integrity("second")
        val provider = MlpaTokenProvider.choose(
            { Result.failure(FxaNotSignedIn()) },
            { Result.success(expected) },
        )

        assertEquals(expected, provider.fetchToken().getOrThrow())
    }

    @Test
    fun `GIVEN all providers fail with uncategorized errors WHEN fetching THEN the last failure is surfaced`() = runTest {
        val terminal = IllegalStateException("integrity failed")
        val provider = MlpaTokenProvider.choose(
            { Result.failure(FxaNotSignedIn()) },
            { Result.failure(terminal) },
        )

        val result = provider.fetchToken()

        assertTrue(result.isFailure)
        assertSame(terminal, result.exceptionOrNull())
    }

    @Test
    fun `GIVEN an auth failure and an attestation failure WHEN fetching THEN the auth failure wins regardless of order`() = runTest {
        val authFailure = FxaTokenUnavailable()
        // auth failure is tried last, yet still wins: precedence is by category, not position.
        val provider = MlpaTokenProvider.choose(
            { Result.failure(FakeAttestationFailure()) },
            { Result.failure(authFailure) },
        )

        assertSame(authFailure, provider.fetchToken().exceptionOrNull())
    }

    @Test
    fun `GIVEN an attestation failure and an uncategorized failure WHEN fetching THEN the attestation failure wins over the last fallback`() = runTest {
        val attestationFailure = FakeAttestationFailure()
        // the uncategorized failure is tried last, but the attestation failure is still surfaced.
        val provider = MlpaTokenProvider.choose(
            { Result.failure(attestationFailure) },
            { Result.failure(FxaNotSignedIn()) },
        )

        assertSame(attestationFailure, provider.fetchToken().exceptionOrNull())
    }

    @Test
    fun `GIVEN no providers WHEN choosing THEN it throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            MlpaTokenProvider.choose()
        }
    }

    @Test
    fun `GIVEN an available token WHEN fetching via fxaTokenProvider THEN it succeeds with an fxa token`() = runTest {
        val provider = MlpaTokenProvider.fxaTokenProvider { FxaAccessToken.Available("token") }

        assertTrue(provider.fetchToken().getOrNull() is AuthorizationToken.Fxa)
    }

    @Test
    fun `GIVEN no signed-in account WHEN fetching via fxaTokenProvider THEN it fails with FxaNotSignedIn`() = runTest {
        val provider = MlpaTokenProvider.fxaTokenProvider { FxaAccessToken.NotSignedIn }

        assertTrue(provider.fetchToken().exceptionOrNull() is FxaNotSignedIn)
    }

    @Test
    fun `GIVEN a signed-in account without a token WHEN fetching via fxaTokenProvider THEN it fails with an auth failure`() = runTest {
        val provider = MlpaTokenProvider.fxaTokenProvider { FxaAccessToken.Unavailable }

        val error = provider.fetchToken().exceptionOrNull()
        assertTrue(error is FxaTokenUnavailable)
        assertTrue(error is AuthFailure)
    }
}

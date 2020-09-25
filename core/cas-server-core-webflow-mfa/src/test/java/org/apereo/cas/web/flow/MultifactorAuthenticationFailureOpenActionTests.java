package org.apereo.cas.web.flow;

import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * This is {@link MultifactorAuthenticationFailureOpenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
public class MultifactorAuthenticationFailureOpenActionTests extends MultifactorAuthenticationFailureActionTests {
    @Override
    @Test
    public void verifyOperations() throws Exception {
        executeAction(RegisteredServiceMultifactorPolicyFailureModes.OPEN, CasWebflowConstants.TRANSITION_ID_BYPASS);
    }
}

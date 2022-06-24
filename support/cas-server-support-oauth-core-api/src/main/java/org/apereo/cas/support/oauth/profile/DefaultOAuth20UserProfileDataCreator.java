package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link OAuth20UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultOAuth20UserProfileDataCreator<T extends OAuth20ConfigurationContext>
    implements OAuth20UserProfileDataCreator {

    private final ObjectProvider<T> configurationContext;

    @Override
    @Audit(action = AuditableActions.OAUTH2_USER_PROFILE,
        actionResolverName = AuditActionResolvers.OAUTH2_USER_PROFILE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_USER_PROFILE_RESOURCE_RESOLVER)
    public Map<String, Object> createFrom(final OAuth20AccessToken accessToken, final JEEContext context) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getObject().getServicesManager(), accessToken.getClientId());

        val principal = getAccessTokenAuthenticationPrincipal(accessToken, context, registeredService);
        val modelAttributes = new HashMap<String, Object>();
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, principal.getId());
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID, accessToken.getClientId());
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES, collectAttributes(principal));
        finalizeProfileResponse(accessToken, modelAttributes, principal, registeredService);
        return modelAttributes;
    }

    protected Map<String, List<Object>> collectAttributes(final Principal principal) {
        return principal.getAttributes();
    }

    protected Principal getAccessTokenAuthenticationPrincipal(final OAuth20AccessToken accessToken,
                                                              final JEEContext context,
                                                              final RegisteredService registeredService) {
        val currentPrincipal = accessToken.getAuthentication().getPrincipal();
        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", currentPrincipal);
        val principal = configurationContext.getObject().getProfileScopeToAttributesFilter().filter(
            accessToken.getService(), currentPrincipal, registeredService, accessToken);
        LOGGER.debug("Created CAS principal [{}] based on requested/authorized scopes", principal);
        return principal;
    }

    protected void finalizeProfileResponse(final OAuth20AccessToken accessTokenTicket,
                                           final Map<String, Object> modelAttributes,
                                           final Principal principal,
                                           final RegisteredService registeredService) {
        if (registeredService instanceof OAuthRegisteredService) {
            val service = accessTokenTicket.getService();
            modelAttributes.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
    }
}

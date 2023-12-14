var randomString = function(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for(var i = 0; i < length; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
}

// same blacklist as in rights_roles.handler
var params_blacklist=["code", "state", "session_state"];

function explorama_keycloak_auth(auth_url, clid) {
    sso_url = auth_url + '?scope=openid&client_id=' 
        + clid + '&state=' + randomString(10) + '&response_type=code&redirect_uri=' + window.location.href;
    window.location.replace(sso_url);
}

function reduce_url_params (params) {
    for (const param_key of params_blacklist) {
      params.delete(param_key);
      }
}

function explorama_keycloak_auth_validate_sso() {
    const queryString = window.location.search;
    const params = new URLSearchParams(queryString);
    const code = params.get("code");
    if (code && code.length > 0) {
        reduce_url_params(params);
        let url_params = params.toString()
        url_params = url_params.length == 0 ? "" : "?" + url_params;
        document.getElementById('sso-query-string').value = queryString.substring(1);
        document.getElementById('redirect-url').value = window.location.origin + window.location.pathname + url_params;
        document.getElementById('sso-login-form').submit();
    }
}
window.onload = explorama_keycloak_auth_validate_sso

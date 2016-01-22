# Authentication and Authorization

Authentication and authorization are usually confused because they are intimately tied together. However, they need to be distinguished. Authentication establishes your identity against a system whilst authorization establishes what you (once identified) are allowed to do with the system. Generally authentication and authorization schemes work by exchanging information that is assumed to only be sourced from the user. Some examples of these are username-password credentials. Some authentication systems assume that these pair of values can only be known and be provided by some particular person. Thus, when such are provided, these authentication system would assume the login actor to be the same user.

## OAuth2

OAuth2 is properly an authorization protocol. It delegates the authentication to a 3rd party systems and thus, it only specifies the conversations between system that would allow/disallow interaction with other systems or actor. The protocol has mainly 3 exchanges:
 1. Application requests User (or resource owner) to be allowed/granted access to resources.
 2. Application uses these authorization grants to requests access tokens from Authorization server.
 3. The Application now requests for resources from the Resource server passing along the access tokens.

## OAuth2 vis-a-vis Session Cookies

Although very similar with session cookies, OAuth tokens relieve the server the need to maintain the state of the tokens since tokens are self contained. In fact, OAuth provides for different types of tokens depending on their validity. Given a token, the server only needs to validate it. Session cookies on the other hand require the same server to maintain the state of the session.

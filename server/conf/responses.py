from quart import jsonify



class ApiResponse:

    def __init__(self, message: str="Success!", code: int=404):
        self.message = message
        self.code = code
        self.status = 200

    @property
    def response(self):
        return jsonify({
            "status": self.code,
            "message": self.message
        }), self.status



    @property
    def registered(self):
        self.message = "User has been registered."
        self.code = 100
        return self.response

    @property
    def authorized(self):
        self.message = "User has been authorized."
        self.code = 101
        return self.response



    @property
    def request_limit(self):
        self.message = "You've reached a limit for that request."
        self.code = 300
        return self.response



    @property
    def unsupported_method(self):
        self.message = "Method isn't supported."
        self.code = 400
        self.status = 400
        return self.response

    @property
    def noauthdata(self):
        self.message = "Login or Password weren't specified."
        self.code = 401
        return self.response

    @property
    def unauthorized(self):
        self.message = "Unauthorized."
        self.code = 402
        return self.response

    @property
    def unregistered(self):
        self.message = "Unregistered. Change your login."
        self.code = 403
        return self.response



    @property
    def api_error(self):
        self.message = "API crashed.. :("
        self.code = 501
        return self.response

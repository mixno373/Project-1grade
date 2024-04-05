import os, sys
import locale

from quart import Quart, request, jsonify, send_file
from quart_cors import cors

from conf.const import *
from config.settings import settings
from conf.classes import *
from conf.responses import ApiResponse



__name__ = "Discoin-API"
__version__ = "0.1.0"

app = Quart(__name__)
app = cors(app, allow_origin="*")

APIRESP = ApiResponse()

if sys.platform == 'win32':
    locale.setlocale(locale.LC_ALL, 'rus_rus')
else:
    locale.setlocale(locale.LC_ALL, 'ru_RU.UTF-8')
    
    
@app.before_first_request
async def create_db():
    try:
        app.pool = PostgresqlDatabase(dsn=settings['psql'])
        await app.pool.connect()
        print('PostgreSQL successfully loaded!')
    except Exception as e:
        print('PostgreSQL doesn\'t load.\n'+str(e))
        exit(0)
        
        
        
@app.route('/discoin/auth', methods=HTTP_METHODS)
async def auth_():
    if request.method == 'GET':
        headers, data, args, form = await request_parse(request)

        data_stor = [data, args, form]

        login = form_data_get(data_stor, "login", "-").lower()
        access_code = form_data_get(data_stor, "access_code", "-")
        
        resp = {
            "balance": 1_000_000,
            "profit": 373,
        }
        
        # resp = await auth_user(app.pool, login, password, state)

        return jsonify(resp), 200

    return APIRESP.unsupported_method




app.run(host='localhost', port=8083, debug=False, use_reloader=True)
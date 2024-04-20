import os, sys
import locale, json

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
    
    
@app.before_serving
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
    if request.method in ['GET', 'POST']:
        headers, data, args, form = await request_parse(request)

        data_stor = [data, args, form]

        login = form_data_get(data_stor, "login", "-").lower()
        access_code = form_data_get(data_stor, "access_code", "-")
        
        u_data = await app.pool.select("*", "users", where={"login": login})
        
        if u_data:
            if u_data["access_code"] != access_code: return APIRESP.unauthorized
            
            return APIRESP.authorized
        
            
        if len(login) < 3 or len(login) > 15: return APIRESP.unregistered
        if len(access_code) != 25: return APIRESP.unregistered
        
        await app.pool.insert({
            "login": login,
            "access_code": access_code,
            "balance": 0,
            "items": '[]',
            "ts": unix_time(),
        }, "users")

        return APIRESP.registered

    return APIRESP.unsupported_method


@app.route('/discoin/wallet', methods=HTTP_METHODS)
async def wallet_():
    if request.method in ['GET', 'POST']:
        headers, data, args, form = await request_parse(request)

        data_stor = [data, args, form]

        login = form_data_get(data_stor, "login", "-").lower()
        access_code = form_data_get(data_stor, "access_code", "-")
        
        resp = {
            "balance": 0,
            "profit": 0,
            "ts": unix_time(),
            "items": []
        }
        
        u_data = await app.pool.select("*", "users", where={"login": login, "access_code": access_code})
        
        if not u_data: return resp   # APIRESP.unauthorized
        
        profit = 1
        for item_ in json.loads(u_data["items"]):
            profit += item_["count"] * item_["profit"]
            resp["items"].append(item_)
        
        ts = unix_time()
        
        resp["profit"] = profit
        resp["balance"] = u_data["balance"] + (ts - u_data["ts"]) * profit
        resp["ts"] = ts
        
        await app.pool.update({
            "balance": resp["balance"],
            "ts": ts,
        }, "users", where={"login": login})

        return jsonify(resp), 200

    return APIRESP.unsupported_method




app.run(host='localhost', port=8083, debug=False, use_reloader=True)
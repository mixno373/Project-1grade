import os, sys
import locale, json, copy

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
        
        u_data, ts = await update_balance(app, login)
        if ts > 0:
            if u_data["access_code"] != access_code: return APIRESP.unauthorized
            
            return APIRESP.authorized
        
            
        if len(login) < 3 or len(login) > 15: return APIRESP.unregistered
        if len(access_code) != 25: return APIRESP.unregistered
        
        await app.pool.insert({
            "login": login,
            "access_code": access_code,
            "balance": 0,
            "profit": 1,
            "items": json.dumps([WalletItems[0]], ensure_ascii=False, sort_keys=True, separators=(',', ':')),
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
        
        u_data, ts = await update_balance(app, login, access_code)
        if ts < 0: return u_data   # APIRESP.unauthorized
        
        resp = {
            "balance": u_data["balance"],
            "profit": u_data["profit"],
            "ts": u_data["ts"],
            "items": json.loads(u_data["items"])
        }

        return jsonify(resp), 200

    return APIRESP.unsupported_method


@app.route('/discoin/buyitem', methods=HTTP_METHODS)
async def buyitem_():
    if request.method in ['GET', 'POST']:
        headers, data, args, form = await request_parse(request)

        data_stor = [data, args, form]

        login = form_data_get(data_stor, "login", "-").lower()
        access_code = form_data_get(data_stor, "access_code", "-")
        name = form_data_get(data_stor, "name", "-").lower()
        count = int(form_data_get(data_stor, "count", "1"))
        
        if count < 1: count = 1
        
        item = None
        for i in WalletItems:
            if i["name"].lower() == name:
                item = i
                break
        else:
            return APIRESP.not_item
        
        u_data, ts = await update_balance(app, login, access_code)
        if ts < 0: return u_data   # APIRESP.unauthorized
        
        
        profit = 1
        items = []
        old_items = {oi['name'].lower(): oi for oi in json.loads(u_data["items"])}
        for w_item in WalletItems:
            item_ = old_items.get(w_item["name"].lower())
            
            if item_ and item_["name"].lower() != name:
                profit += item_["profit"] * item_["count"]
                items.append(item_)
                continue
            
            if w_item["name"].lower() != name:
                continue
            
            if not item_:
                item_ = copy.deepcopy(w_item)
            
            count = min(count, int(u_data["balance"] / item_["cost"]))
            count = min(item["max"] - item_["count"], count)
            item_["count"] = min(item_["count"] + count, item["max"])
            profit += item_["profit"] * item_["count"]
            items.append(item_)
            
        old_items = {oi['name'].lower(): oi for oi in items}
        for w_item in WalletItems:
            item_ = old_items.get(w_item["name"].lower())
            if not item_:
                items.append(copy.deepcopy(w_item))
                break
            
            
        u_data = await app.pool.update({
            "balance": u_data["balance"] - count * item["cost"],
            "profit": profit,
            "items": json.dumps(items, ensure_ascii=False, sort_keys=True, separators=(',', ':')),
            "ts": ts,
        }, "users", where={"login": login, "access_code": access_code})
        
        resp = {
            "balance": u_data["balance"],
            "profit": u_data["profit"],
            "ts": u_data["ts"],
            "items": items
        }

        return jsonify(resp), 200

    return APIRESP.unsupported_method




app.run(host='localhost', port=8083, debug=False, use_reloader=True)
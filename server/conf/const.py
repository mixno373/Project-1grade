import random, re
from datetime import datetime, timedelta



random.seed()

HTTP_METHODS = ['GET', 'HEAD', 'POST', 'PUT', 'DELETE', 'CONNECT', 'OPTIONS', 'TRACE', 'PATCH']


def unix_time():
    return int(datetime.utcnow().timestamp())

def datenow():
    return datetime.utcnow() + timedelta(hours=8)

def split_int(value, char = "."):
    char = str(char)
    value = str(value)
    pattern = "([0-9]{3})"
    value = value[::-1]
    value = re.sub(pattern, r"\1"+char, value)
    value = value[::-1]
    value = value.lstrip(char)
    return value

async def request_parse(request):
    headers = request.headers
    data = await request.json
    args = request.args
    form = await request.form

    if not data:
        data = {}

    for key, value in args.items():
        data[key] = value

    return headers, data, args, form

def get_int_from_data(data, key, min=1, max=1, default=1):
    try:
        value = int(data[key])
    except:
        value = default
    if value > max:
        value = max
    if value < min:
        value = min
    return value

def form_data_get(datas: list, key: str, default_val = "-"):
    value = None
    for data in datas:
        try:
            value = data.get(key)
            if value != None:
                return value
        except:
            value = None
    if value == None:
        value = default_val
    return value

async def update_balance(app, login: str, access_code: str=None):
    ts = unix_time()
    params = {"login": login}
    if access_code: params["access_code"] = access_code
    u_data = await app.pool.update(f"balance = balance + ({ts} - ts) * profit, ts = {ts}", "users", where=params)
    if not u_data: return ({"balance":0,"profit":0,"ts":0,"items":[]}, -1)   # APIRESP.unauthorized
    return (u_data, ts)


WalletItems = [    
    {
        "name": "Монетка", 
        "count": 0,  
        "profit": 1,   
        "cost": 5,    
        "max": 150
    },
    {
        "name": "Майнер",  
        "count": 0,   
        "profit": 2,    
        "cost": 55,     
        "max": 100
    },
    {
        "name": "Зимняя заначка",  
        "count": 0,   
        "profit": 3,    
        "cost": 1500,     
        "max": 50
    },
    {
        "name": "Золотой слиток",  
        "count": 0,   
        "profit": 3,    
        "cost": 3500,     
        "max": 41
    },
    {
        "name": "Центральный Банк",  
        "count": 0,   
        "profit": 4,   
        "cost": 6000,     
        "max": 36
    },
    {
        "name": "Денежный Арсенал",  
        "count": 0,   
        "profit": 5,    
        "cost": 8000,     
        "max": 30
    },
    {
        "name": "Тайник Императора",  
        "count": 0,   
        "profit": 6,    
        "cost": 10000,     
        "max": 25
    },
    {
        "name": "Неприкосновенный Запас",  
        "count": 0,   
        "profit": 8,    
        "cost": 100000,     
        "max": 20
    },
    {
        "name": "Драконий Сейф",  
        "count": 0,   
        "profit": 9,    
        "cost": 250000,     
        "max": 15
    },
    {
        "name": "Казначейский Сундук",  
        "count": 0,   
        "profit": 10,    
        "cost": 700000,     
        "max": 12
    },
    {
        "name": "Золотовалютный Резерв",  
        "count": 0,   
        "profit": 11,    
        "cost": 1000000,     
        "max": 5
    },
    {
        "name": "Криптовалютный Кошель",  
        "count": 0,   
        "profit": 13,    
        "cost": 3500000,     
        "max": 3
    },
    {
        "name": "Сокровищница Миллионера",  
        "count": 0,   
        "profit": 15,    
        "cost": 5000000,     
        "max": 2
    },
    {
        "name": "Денежная Империя",  
        "count": 0,   
        "profit": 25,    
        "cost": 10000000,     
        "max": 1
    }
]
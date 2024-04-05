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
import asyncpg



class PostgresqlDatabase:

    def __init__(self, dsn):
        self._dsn = dsn
        self.pool = None

    async def connect(self):
        self.pool = await asyncpg.create_pool(dsn=self._dsn, command_timeout=60)

    def clear(self, value):
        return str(value).replace('\\', '\\\\').replace('\'', '\\\'').replace('\"', '\\\"')

    def _where(self, where, table=None):
        w = ""
        if isinstance(where, dict):
            args = []
            for key, value in where.items():
                if table:
                    key = f"{table}.{key}"
                if isinstance(value, int) or isinstance(value, float):
                    arg = f"{key}={value}"
                else:
                    arg = f"{key}=E'{self.clear(value)}'"
                args.append(arg)
            if args:
                w = "WHERE " + " AND ".join(args)
        else:
            w = str(where)
        return w

    def _target(self, target):
        t = ""
        if isinstance(target, list):
            t = ", ".join(target)
        elif target:
            t = str(target)
        else:
            t = "*"
        return t

    def _order(self, order):
        o = ""
        if isinstance(order, dict):
            args = []
            for key, value in order.items():
                if value:
                    arg =f"{key} ASC"
                else:
                    arg = f"{key} DESC"
                args.append(arg)
            if args:
                o = "ORDER BY " + ", ".join(args)
        return o

    def _limit(self, limit):
        l = ""
        if limit > 0:
            l = f"LIMIT {int(limit)}"
        return l

    def _offset(self, offset):
        of = ""
        if offset > 0:
            of = f"OFFSET {int(offset)}"
        return of

    async def fetchrow(self, *args, **kwargs):
        return await self.pool.fetchrow(*args, **kwargs)

    async def fetch(self, *args, **kwargs):
        return await self.pool.fetch(*args, **kwargs)

    async def execute(self, *args, **kwargs):
        return await self.pool.execute(*args, **kwargs)

    async def select(self, target, table: str, where={}, order={}, offset=0):
        t = self._target(target)
        w = self._where(where)
        o = self._order(order)
        of = self._offset(offset)
        return await self.pool.fetchrow(f"""SELECT {t} FROM {table} {w} {o} {of};""")

    async def select_all(self, target, table: str, where={}, order={}, limit=0, offset=0):
        t = self._target(target)
        w = self._where(where)
        o = self._order(order)
        l = self._limit(limit)
        of = self._offset(offset)
        return await self.pool.fetch(f"""SELECT {t} FROM {table} {w} {o} {l} {of};""")
    
    async def delete_from(self, table: str, where={}):
        w = self._where(where)
        return await self.pool.execute(f"""DELETE FROM {table} {w};""")

    async def insert(self, target: dict, table):
        assert target, "Values is None"

        names = []
        values = []
        for key, value in target.items():
            names.append(str(key))
            if isinstance(value, int) or isinstance(value, float):
                values.append(str(value))
            elif isinstance(value, bool):
                values.append("TRUE" if value else "FALSE")
            elif value == None:
                values.append("NULL")
            elif isinstance(value, list):
                v = []
                for val in value:
                    if isinstance(val, int):
                        v.append(str(val))
                    elif isinstance(val, bool):
                        v.append("TRUE" if val else "FALSE")
                    elif val == None:
                        v.append("NULL")
                    else:
                        v.append(f"E'{self.clear(val)}'")
                value = ",".join(v)
                values.append(f"ARRAY[{value}]")
            else:
                values.append(f"E'{self.clear(value)}'")
        names = ",".join(names)
        values = ",".join(values)
        expression = f"""INSERT INTO {table}({names}) VALUES ({values}) RETURNING *;"""
        # print(expression)
        return await self.pool.fetchrow(expression)

    async def insert_update(self, target: dict, table: str, constraint: str=None, where={}, column: str=None):
        assert target, "Values is None"

        names = []
        values = []
        for key, value in target.items():
            names.append(str(key))
            if isinstance(value, int) or isinstance(value, float):
                values.append(str(value))
            elif isinstance(value, bool):
                values.append("TRUE" if value else "FALSE")
            elif value == None:
                values.append("NULL")
            elif isinstance(value, list):
                v = []
                for val in value:
                    if isinstance(val, int):
                        v.append(str(val))
                    elif isinstance(val, bool):
                        v.append("TRUE" if val else "FALSE")
                    elif val == None:
                        v.append("NULL")
                    else:
                        v.append(f"E'{self.clear(val)}'")
                value = ",".join(v)
                values.append(f"ARRAY[{value}]")
            elif isinstance(value, dict):
                for _k, _v in value.items():
                    values.append(f"{_k}")
                    break
            else:
                values.append(f"E'{self.clear(value)}'")
        names = ",".join(names)
        values = ",".join(values)
        s = []
        for key, value in target.items():
            if isinstance(value, int) or isinstance(value, float):
                val = value
            elif isinstance(value, bool):
                val = "TRUE" if value else "FALSE"
            elif value == None:
                val = "NULL"
            elif isinstance(value, list):
                v = []
                for val_ in value:
                    if isinstance(val_, int):
                        v.append(str(val_))
                    elif isinstance(val_, bool):
                        v.append("TRUE" if val_ else "FALSE")
                    elif val_ == None:
                        v.append("NULL")
                    else:
                        v.append(f"E'{self.clear(val_)}'")
                value = ",".join(v)
                val = f"ARRAY[{value}]"
            elif isinstance(value, dict):
                for _k, _v in value.items():
                    val = f"{table}.{key}{_v}{_k}"
            else:
                val = f"E'{self.clear(value)}'"
            s.append(f"{key}={val}")
        s = ",".join(s)
        c = ""
        if constraint:
            c = f"ON CONSTRAINT {constraint}"
        w = self._where(where, table)
        if column:
            column = f"({column})"
        else:
            column = ""
        expression = f"""INSERT INTO {table}({names}) VALUES ({values}) ON CONFLICT {column} {c} DO UPDATE SET {s} {w} RETURNING *;"""
        # print(expression)
        return await self.pool.fetchrow(expression)

    async def update(self, target: dict, table: str, where={}):
        assert target, "Values is None"

        w = self._where(where)
        s = []
        for key, value in target.items():
            if isinstance(value, int) or isinstance(value, float):
                val = value
            elif isinstance(value, bool):
                val = "TRUE" if value else "FALSE"
            elif value == None:
                val = "NULL"
            elif isinstance(value, dict):
                for _k, _v in value.items():
                    val = f"{table}.{key}{_v}{_k}"
            else:
                val = f"E'{self.clear(value)}'"
            s.append(f"{key}={val}")
        s = ",".join(s)
        expression = f"""UPDATE {table} SET {s} {w} RETURNING *;"""
        # print(expression)
        return await self.pool.fetchrow(expression)

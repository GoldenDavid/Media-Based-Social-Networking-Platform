import zipfile, sys

with zipfile.ZipFile('/app/app.jar') as z:
    names = z.namelist()
    for n in names:
        if 'SessionConfig' in n and n.endswith('.class'):
            print('Found:', n)
            data = z.read(n)
            if b'EnableRedisHttpSession' in data:
                print('HAS @EnableRedisHttpSession')
            else:
                print('MISSING @EnableRedisHttpSession')
        if 'socialnetwork-common' in n and n.endswith('.jar'):
            print('Inner jar:', n)
            inner_data = z.read(n)
            import io
            with zipfile.ZipFile(io.BytesIO(inner_data)) as iz:
                for iname in iz.namelist():
                    if 'SessionConfig' in iname:
                        idata = iz.read(iname)
                        print('  Inner:', iname, len(idata), 'bytes')
                        if b'EnableRedisHttpSession' in idata:
                            print('  HAS @EnableRedisHttpSession')
                        else:
                            print('  MISSING @EnableRedisHttpSession')
            break

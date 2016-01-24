import redis
import datetime
import re

r = redis.StrictRedis(host='localhost', port=6379, db=0)

with open('my-dictionary', 'r') as dictionary:
    content =  dictionary.read()
    m = re.findall("\*\*([^*]*)\n+", content)
    for match in m:
        # TODO: lowercase all words
        # TODO: make sure first line only contains one word
        # TODO: strip empty lines in note
        split_results = match.split("\n", 1)
        word = split_results[0].strip()
        note = split_results[1]
        print "Found word {0}".format(word)
        print "Found note {0}".format(note)
        data = {
            "note": note,
            "insert_date": datetime.date.today(),
            "repetition": 0
        }
        # TODO: do not insert if word is already in redis
        r.hmset(word, data)


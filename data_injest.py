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
        data = {
            "note": note,
            "review_date": datetime.date.today(),
            "repetition": 0
        }
        if r.hkeys(word):
            print "The word '{0}' already exists. Do not modify it".format(word)
        else:
            print "Inserting word {'0'}".format(word)
            r.hmset(word, data)

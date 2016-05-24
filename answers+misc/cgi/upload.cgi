#!/pkg/p/Python-2.0/bin/python

"""
This program executes authentication and prompts the user
to upload answers for senseval
"""

import traceback

try:
    #
    # system mods
    #
    import cgi
    import os
    import sys
    import stat
    import time
    
    #
    # project mods
    # 
    import util
    import fmts

    def present_auth(err=""):
        util.cgihdr()
        print fmts.upload % vars()

    #
    # this authenticates and also uploads the file all at once
    #
    def check_and_upload(input):
        try:
            uname = input["username"].value
            user = util.User(uname)
            pw = input["password"].value
            if user.checkpw(pw):
                # upload the file
                infile = input["answers"].file
                sysname = input["system"].value
                user.upload_answers(infile, sysname)
                # print out thanks etc
                util.cgihdr()
                print fmts.upload_thanks
            else:
                raise util.InvalidPW()  # jump to below, invalid pw 
        except (KeyError, util.InvalidPW):
            present_auth(err=('<h2><font color="#ff0000">'
                              'Sorry -- invalid login or incomplete form'
                              '</font></h2>'))

    def dispatch():
        input = cgi.FieldStorage()
        if not input.keys():
            present_auth()
        elif input.has_key("username") and input.has_key("password"):
            check_and_upload(input)
        else:
            # XXX we don't get here unless they post arbitrary things
            # we didn't ask for.
            util.cgihdr()
            print "Please use the interface presented."
    
    dispatch()

except:
    print "Content-type text/html"
    print 
    print "<pre>"
    t, v, tb = sys.exc_info()
    traceback.print_exception(t, v, tb, file=sys.stdout)
    print "</pre>"
    print "Sorry, An error occurred!"

    








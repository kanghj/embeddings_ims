#!/pkg/p/Python-2.0/bin/python

"""
This program executes authentication and retrieves the
next appropriate download data for a participants in senseval.
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

    def download_hdr(filename):
        print "Content-type: application/data"
        print "Content-disposition: inline;filename=%s" % filename
        print
        
    def present_auth(err=""):
        util.cgihdr()
        print fmts.authenticate % {"auth_action" : "download.cgi",
                                   "err": err}

    def present_unavailable(stage, task):
        util.cgihdr()
        print fmts.stage_unavailable % vars()

    def present_available(user, stage, task):
        print "Pragma: no-cache"
        util.cgihdr()
        print fmts.stage_available % vars()

    def clean_reserved():
        # clean out old files here
        dir = os.path.join(util.ROOTDIR, "reserve-auth")        
        for f in os.listdir(dir):
            path = os.path.join(dir, f)
            ctime = os.stat(path)[stat.ST_CTIME]
            if time.time() - util.reserve_auth_time > ctime:
                util.log("cleaning out %s", path)
                os.unlink(path)
                
    def get_reserve_path(uname, stage):
        dir = os.path.join(util.ROOTDIR, "reserve-auth")
        path = os.path.join(dir, "%s-%s" % (uname, stage))
        return path

    def reserve_auth(uname, stage):
        util.log("reserving authentication for %s %s", uname, stage)
        path = get_reserve_path(uname, stage)
        if not os.path.exists(path):
            fp = open(path, "w")
            fp.close()
            os.chmod(path, 0000)
        clean_reserved()

    def check_reserved(uname, stage):
        path = get_reserve_path(uname, stage)
        clean_reserved()
        if os.path.exists(path):
            return 1
        return 0

    def authenticate(input):
        try:
            uname = input["username"].value
            user = util.User(uname)
            pw = input["password"].value
            if user.checkpw(pw):
                stage = user.get_remaining_stage()
                task = user.task
                reserve_auth(user.username, stage)
                if util.stage_available(stage, task):
                    present_available(user.username, stage, task)
                else:
                    present_unavailable(stage, task)
            else:
                raise util.InvalidPW()  # jump to below, invalid pw 
        except (KeyError, util.InvalidPW):
            present_auth(err=('<h2><font color="#ff0000">'
                              'Sorry -- invalid login</font></h2>'))

    def dispatch():
        input = cgi.FieldStorage()
        if not input.keys():
            present_auth()
        elif input.has_key("username") and input.has_key("password"):
            authenticate(input)
        elif input.has_key("user") and input.has_key("stage"):
            uname = input["user"].value
            stage = input["stage"].value
            if check_reserved(uname, stage):
                user = util.User(uname)

                download_hdr(user.get_filename(stage))
                sys.stdout.write(user.get_data_for_stage(stage))
            else:
                errmsg = '''
                <h2><font color="#ff0000"> Sorry -- you\'re
                no  longer authenticated, please try again
                '''
                present_auth(err=errmsg)
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

    








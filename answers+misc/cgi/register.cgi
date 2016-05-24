#!/pkg/p/Python-2.0/bin/python

import traceback

try:
    import string
    import cgi
    import re
    import sys
    import os
    
    import util
    import fmts
    
    fields = {
        "username": 1,
        "password": 1,
        "password2": 1,
        "email": 1,
        "organization": 0,
        "task": 1
        }
    
    def mktask_opts():
        options = []
        for val, descr in util.tasks:
            options.append('\t<option name="task" value="%s">'
                           '%s</option>' % (val, descr))
        return "\n".join(options)

    def present_form(error=""):
        task_options = mktask_opts()
        print fmts.form % vars()

    def check_input(input):
        errs = []
        for fieldname, required in fields.items():
            if required and not input.has_key(fieldname):
                errs.append("Must fill out %s" % (fieldname))
            else:
                if input.has_key(fieldname):
                    val = input[fieldname].value
                    if type(val) == type([]):
                        errs.append("Too many values associated with %s" % \
                                    (fieldname))
                        
        if not errs:
            errs.extend(util.check_username(input['username'].value))
        emailprg = re.compile(r"[a-z0-9_.-]+@([a-z-]+\.){1,}[a-z]{2,3}$",
                              re.I)
        if input.has_key('email') and \
           not emailprg.match(input['email'].value):
            errs.append("email address '%s' doesn't look good." % \
                        (input['email'].value))
        if input.has_key('password') and input.has_key('password2') and \
           input['password'].value != input['password2'].value:
            errs.append("passwords don't match")
        return errs

    def save_input(input):
        if input.has_key('organization'):
            org = input['organization'].value
        else:
            org = ""
        org = org.replace('\n', ' ')
        org = org.replace(':', '__COLON__')
        if input.has_key('systems'):
            systems=input['systems'].value
            systems=systems.replace(" ", "")
            systems=systems.replace("\t", "")
        else:
            systems = ""
        util.newuser(input['username'].value,
                     input['email'].value,
                     systems,
                     input['password'].value,
                     input['task'].value,
                     org)

    def dispatch():
        util.cgihdr()
        input = cgi.FieldStorage()
        if not input.keys():
            present_form()
            sys.exit(0)
        errs = check_input(input)
        if errs:
            errs = '<font color="#ff0000">Error!<br> <ul><li>%s</ul>' % \
                   ('\n<li>'.join(errs))
            errs = errs + ('Please click "back" on your browser and '
                           'try again</font>')
            present_form(errs)
        else:
            save_input(input)
            print fmts.thanks

    dispatch()
    
except SystemExit:
    pass
except:
    util.cgihdr()
    print "<pre>"
    t, v, tb = sys.exc_info()
    traceback.print_exception(t,v, tb, file=sys.stdout)
    print "</pre>"
    print "Sorry, An error occurred!"
    

    



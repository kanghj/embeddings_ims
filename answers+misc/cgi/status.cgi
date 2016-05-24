#!/pkg/p/Python-2.0/bin/python

# -*- python -*-
"""
This program simply prints out the status of each task so that participants
can quickly see what's available.
"""

try:
    import util
    #
    # make a row in an html table showing the task and the
    # data available for it.
    #
    def statrow(task, taskname):
        s = "<td>%s</td>" % taskname
        for stage_name in "trial", "train", "test":
            if util.stage_available(stage_name, task):
                s = s + '<td> <font color="#00aa00">available</font></td>'
            else:
                s = s + '<td> <font color="#ff0000">not available</font></td>'
        return "<tr>%s</tr>\n" % s


    #
    # make an html table using the rows from statrow()
    #
    def mktable():
        table_rows = []
        for t, tname in util.tasks:
            table_rows.append(statrow(t, tname))
        return '''<table><tr><th>Task</th><th>Trial Data</th><th>Train Data</th>
        <th>Test Data</th></tr>%s</table>''' % '\n'.join(table_rows)

    #
    # main entry point to control flow
    #
    print "Content-type: text/html"
    print
    print "<html><head><title>Senseval Task Download Data Status</title></head>"
    print '<body bgcolor="#ffffff">'
    print '<center>'
    print '''<h3>Senseval Task Download Data Status</h2>'''
    print mktable()
            
except:
    #
    # in case something goes wrong, make an informative error message
    # to help debug shit.
    #
    import sys, traceback
    print "Content-type text/html"
    print 
    print "<pre>"
    t, v, tb = sys.exc_info()
    traceback.print_exception(t, v, tb, file=sys.stdout)
    print "</pre>"
    print "Sorry, An error occurred!"
    

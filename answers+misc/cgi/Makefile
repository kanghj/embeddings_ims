#
# edit this to install the files somewhere other than in
# my directory
#
CGIDIR=/home/cotton/public_html/cgi-bin/senseval/
INSTALL=./install-sh
MODMODE=0444
CGIMODE=555

#
GROUP=lib-staf

MODS=util.py fmts.py
CGIS=download.cgi register.cgi status.cgi upload.cgi



default: install

install: mods cgis


mods: $(MODS)
	for mod in $(MODS); do \
		echo "...installing	$$mod"; \
	        $(INSTALL) -c -g $(GROUP) -m $(MODMODE) $$mod $(CGIDIR); \
	done

cgis: $(CGIS)
	for cgi in $(CGIS); do \
		echo "...installing 	$$cgi"; \
		$(INSTALL) -c -g $(GROUP) -m $(CGIMODE) $$cgi $(CGIDIR); \
	done


clean:
	rm -f *~  \#* a.out core *.pyc

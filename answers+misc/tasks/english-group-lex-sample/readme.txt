As per the senseval workshop meeting at ACL 2001, The english lexical sample
task will be re run from the ground up with grouped sense identifiers.  This
was decided because inter annotator agreement scores were much higher with
grouped senses.

In this task, the sense-ids in the training document and the answer key for the
test document are replaced with identifiers for the groups, and a separate file
mapping sense ids to group ids is provided.  This file is called "sense2group".

The fine grained score in this task should correspond to the coarse grained
score in the original english lexical sample task.  This task is just put
together to simplify systems' training on the grouped data.


******************************
Local chain storage (database)
******************************
All valid blocks get stored locally on the device in an SQLite database. Android has code in place to handle all the complicated parts, so using the database after setup consists mainly of writing queries. Please refer to the `Android tutorials <https://developer.android.com/training/basics/data-storage/databases.html>`_ for an explanation on how to use SQLite databases in Android.

The database is set up in a similar way as in the :ipv8-repo:`ipv8 python code <ipv8/attestation/trustchain/database.py>`. The only difference is the added column ``TX_FORMAT``. So the database from the ipv8 implementation in python can be imported trivially into android. The columns correspond to the :ref:`message-structure`, so for inserting it simply needs to parse relevant data from the block. Note that it when receiving raw bytes it always has to be passed to a Protocol Buffers object first before it is added to the database, to ensure that data was received correctly.

Database structure
==================
The table has the following columns:

* ``TX`` - Transaction
* ``TX_FORMAT`` -  Transaction format e.g. ``pdf``
* ``PUBLIC_KEY`` - Base64 encoding of the raw public key pair bytes (see :ref:`crypto`)
* ``SEQUENCE_NUMBER`` - sequence number of the block
* ``LINK_PUBLIC_KEY`` - Base64 encoding of the public key pair of the linked block
* ``LINK_SEQUENCE_NUMBER`` - sequence number of the linked block
* ``PREVIOUS_HASH`` - Base64 encoding of the hash of the previous block in the chain
* ``SIGNATURE`` - Base64 encoding of the signature
* ``INSERT_TIME`` - Time at which the block was inserted into the database
* ``BLOCK_HASH`` - Base64 encoding of the hash

The primary keys are the public key and the sequence number.

Links to code
=============
* :base-repo:`Creation of database, inserting blocks (TrustChainDBHelper.java) <storage/database/TrustChainDBHelper.java>`
* :ipv8-repo:`IPv8 (database.py) <ipv8/attestation/trustchain/database.py>`

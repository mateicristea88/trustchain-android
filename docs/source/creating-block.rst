.. _creating-block-label:

****************
Creating a block
****************
In order to complete a transaction with two parties a block needs to be created. A block in TrustChain is a little different than in bitcoin-style blockchains. In bitcoin-style blockchains, a block is a collection of transactions that happened in the network. A block is created by a node and is propagated through the network. All connected nodes validate the block and the transactions. In TrustChain a block is formed by two peers who wish to agree on a transaction. Therefore a TrustChainBlock only has one transaction.

Both parties need to agree on a transaction, so there has to be some interaction between peers. The way this is done in TrustChain is to first create an incomplete block, called a block proposal. This block proposal is send to the second peer, who completes the block and sends it back to the first peer. This process is explained in more detail below.

Structure of blocks
===================
A block has the following attributes:

* ``public_key`` - The public key of the peer that created this block
* ``sequence_number`` - Represents the position this block has in the chain of the creating peer
* ``link_public_key`` - The public key of the other party
* ``link_sequence_number`` - The position the connected block has in the chain of the other party
* ``previous_hash`` - A sha256 hash of the previous block in the chain
* ``signature`` - The signature of the first peer on the sha256 hash of this block
* ``transaction`` - The data that both parties need to agree on, the input is simply a series of bytes so this can be anything, from text to documents to monetary transactions

Note that ``link_sequence_number`` will be unknown for the created block proposal because peer A won't be sure when peer B inserts the linked block in his chain. This will stay unknown, as updating a block already in the chain is not desirable, since it might invalidate later blocks. When the block is completed peer A will have the block of peer B in its database as well, so it can always find out the position of the linked block in peer B's chain.

Create block
============
There are two situation that require creating a block. Initiating the creation of a transaction with another peer and completing a block that was sent to you by another peer. This is both done using the :base-repo:`TrustChainBlockHelper <block/TrustChainBlockHelper.java>`. This class contains methods for creating, signing, and validating blocks.

Initiating a transaction
------------------------
To initiate a transaction some information is needed: the bytes of the transaction, the initiating party's public key, the public key of the other party, and a link to the database containing your chain to ``createBlock``. The latest block in your chain will be retrieved from the database, to be able to set ``sequence_number`` and ``prev_hash``. ``link_sequence_number`` will remain empty and the hash of this block is calculated and and signed in order to complete the block proposal. The block proposal can now be added to the local chain and send to the other party.

Responding to a block proposal
------------------------------
When a block proposal is received the receiving party B can decide whether or not they agree with the transaction in the block and choose to either sign the block or ignore it. After the block is validated, the party B knows that this proposal is consistent with what it known of the sending party A. In order to complete the block, a new block is created. ``public_key`` and ``sequence_number`` from the block proposal will now be ``link_public_key`` and ``link_sequence_number``. ``public_key``, ``sequence_number``, and ``prev_hash`` will be set according to the current state of party B's chain. ``transaction`` will remain the same and a new hash is calculated that will be signed by party B.

Validate block
==============
Block validation is the most important step here, as this ensures the validity of the blockchain.  There are 6 different validation results:

* ``VALID``
* ``PARTIAL`` - There are gaps between this block and the previous and next
* ``PARTIAL_NEXT`` - There is a gap between this block and the next
* ``PARTIAL_PREVIOUS`` - There is a gap between this block and the previous
* ``NO_INFO`` - We know nothing about this block, it is from an unknown peer, so we can say nothing about its validity
* ``INVALID`` - It violates some rules

The validation function starts of with a valid result and will update the validity result according to whether the rules hold for the block. Validation consists of six steps:

* Step 1: Retrieving all the relevant blocks from the database if they exist (previous, next, linked, this block)
* Step 2: Determine the maximum validity level according to the blocks retrieved in the previous step
* Step 3: Check whether the block is created correctly, e.g. whether it has a sequence number that comes after the sequence number of the genesis block
* Step 4: Check if we already know this block, if so it should be the same as we have in our database
* Step 5: Check if we know the linked block and check if their relation is correct
* Step 6: Check the validity of the previous and next block

For a more detailed explanation of the validation function, please take a look in the code and try to understand what happens there.

Sending a block
===============
There are :ref:`two methods <message-transmission>` for sending a block to another party:

* Via Internet, using the network overlay
* Offline, using either QR codes or Android beam

Note that offline sending will add block to the regular chain, so they will get propagated through the network when the peer is online again.


Links to code
=============
* :base-repo:`Block structure in ProtocolBuffers (Message.proto) <message/Message.proto>`
* :base-repo:`All block related methods (TrustChainBlockHelper.java) <block/TrustChainBlockHelper.java>`

Also see the :ipv8-repo:`readme on the ipv8 github <doc/trustchain.md>`


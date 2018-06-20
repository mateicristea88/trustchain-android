.. _message_structure:

***********************************
Message structure (Protocolbuffers)
***********************************
Creating a network of TrustChain peers which only run a Java version of TrustChain is not very useful. Therefore the TrustChain blocks and messages should be compatible with many platforms, so cross-platform connection is possible. For the storage of the chain this is achieved by using SQLite, which has implementation for many platforms. For sending messages (blocks and crawlrequests) this compatibility can be achieved by using `Google's Protocolbuffers <https://developers.google.com/protocol-buffers/>`_, which is a cross-platform data serialization mechanism. When implementations across platforms use the same Protocolbuffers setup, messaging will across these platforms will be possible.

Making changes
==============
Protocolbuffers is used to create the structure of messages. This includes all the messages used by the network overlay (:ref:`connection`) and the structures of the Trustchain related objects like ``TrustChainBlock`` and ``CrawlRequest``. A complete overview of the message structure can be found in :ref:`complete-structure`. With Protocolbuffers the corresponding Java classes can be compiled, so it is possible to call the structures as objects. Making changes and recompiling the Java classes is quite easy, just follow the `tutorial of ProtocolBuffers <https://developers.google.com/protocol-buffers/docs/javatutorial>`_ and you should be fine. When making changes, don't forget to also update the database structure if necessary.

.. _complete-structure:

Complete structure
==================
The complete structure of all parsable objects as defined in protocolbuffers.

``Message``
-----------
* ``bytes`` source_public_key
* ``string`` source_name
* ``bytes`` destination_address
* ``int32`` destination_port
* ``int32`` type
* ``Payload`` payload

``Payload``
-----------
* ``IntroductionRequest`` introductionRequest
* ``IntroductionResponse`` introductionResponse
* ``Puncture`` puncture
* ``PunctureRequest`` punctureRequest
* ``TrustChainBlock`` block
* ``CrawlRequest`` crawlRequest

``TrustChainBlock``
-----------
* ``bytes`` public_key
* ``int32`` sequence_number
* ``bytes`` link_public_key
* ``int32`` link_sequence_number
* ``bytes`` previous_hash
* ``bytes`` signature
* ``Transaction`` transaction
* ``google.protobuf.Timestamp`` insert_time

``Transaction``
-----------
* ``bytes`` unformatted
* ``string`` format
* ``Claim`` claim

``Claim``
-----------
* ``bytes`` name
* ``google.protobuf.Timestamp`` timestamp
* ``int32`` validity_term
* ``bytes`` proof_format

``CrawlRequest``
-----------
* ``bytes`` public_key
* ``int32`` requested_sequence_number
* ``int32`` limit

``IntroductionRequest``
-----------
* ``int64`` connection_type

``IntroductionResponse``
----------------------------
* ``int64`` connection_type
* ``string`` internal_source_socket
* ``Peer`` invitee
* repeated ``Peer`` peers

``Puncture``
----------------
* ``string`` sourceSocket

``PunctureRequest``
-----------------------
* ``string`` source_socket
* ``Peer`` puncture_peer

``Peer``
------------
* ``bytes`` address
* ``int32`` port
* ``bytes`` public_key
* ``string`` name
* ``int32`` connectionType

Links to code
=============
 * `Structure of message (Message.proto) <https://github.com/klikooo/CS4160-trustchain-android/blob/develop/app/src/main/java/nl/tudelft/cs4160/trustchain_android/message/Message.proto>`_ 


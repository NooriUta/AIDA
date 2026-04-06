-- ArcadeDB seed: initial users
-- Passwords are bcrypt-hashed (cost 12).
-- Plaintext: admin=admin  editor=editor  viewer=viewer
--
-- To generate a new hash: node -e "const b=require('bcryptjs');b.hash('yourpass',12).then(console.log)"

INSERT INTO User SET
  username      = 'admin',
  password_hash = '$2a$12$ef3RxN2n5jjea7.wOnqFQ.MamxaQuip5QOcp2xR0eDOnOwyPoQOwS',
  role          = 'admin';

INSERT INTO User SET
  username      = 'editor',
  password_hash = '$2a$12$KELCp6a40cqWG/IPD97IpOUgPUoBcFuoPKMthM2qre9TxgfwqlyZ6',
  role          = 'editor';

INSERT INTO User SET
  username      = 'viewer',
  password_hash = '$2a$12$RJOGEwUZviLAQ5.SR0kRrexlpEkZi2jqRd2hJwuSd5Z3801kSj776',
  role          = 'viewer';

# Helidon AQ connector demo with OCI
Follow the medium article for setting up free Oracle DB in OCI:
https://medium.com/helidon/helidon-messaging-with-oracle-aq-a023928dbbb8

Setting up aq user:
```sql
create user frank identified by SuperSecretPassword1234;

grant connect to frank;
grant resource to frank;
grant execute on dbms_aq to frank;
grant execute on dbms_aqadm to frank;
grant execute on dbms_aqin to frank;
grant unlimited tablespace to frank;

BEGIN
   ORDS_ADMIN.ENABLE_SCHEMA(
     p_enabled => TRUE,
     p_schema => 'FRANK',
     p_url_mapping_type => 'BASE_PATH',
     p_url_mapping_pattern => 'frank',
     p_auto_rest_auth => TRUE
   );
   COMMIT;
END;
/
```

Create example queues:
```sql
CREATE OR REPLACE PROCEDURE create_queue(queueName IN VARCHAR2, qType IN VARCHAR2) IS
BEGIN
    dbms_aqadm.create_queue_table('FRANK.'||queueName||'_TAB', qType);
    dbms_aqadm.create_queue('FRANK.'||queueName,'FRANK.'||queueName||'_TAB');
    dbms_aqadm.start_queue('FRANK.'||queueName);
END;
/

-- Setup example AQ queues FRANK.EXAMPLE_QUEUE_1, FRANK.EXAMPLE_QUEUE_2, FRANK.EXAMPLE_QUEUE_3
begin
    CREATE_QUEUE('example_queue_1', 'SYS.AQ$_JMS_TEXT_MESSAGE');
    CREATE_QUEUE('example_queue_2', 'SYS.AQ$_JMS_TEXT_MESSAGE');
    CREATE_QUEUE('example_queue_3', 'SYS.AQ$_JMS_TEXT_MESSAGE');
    CREATE_QUEUE('example_queue_bytes', 'SYS.AQ$_JMS_BYTES_MESSAGE');
    CREATE_QUEUE('example_queue_map', 'SYS.AQ$_JMS_MAP_MESSAGE');
end;
/
```

Enqueue text message with PL/SQL
```sql
DECLARE
    enqueue_options    DBMS_AQ.ENQUEUE_OPTIONS_T;
    message_properties DBMS_AQ.MESSAGE_PROPERTIES_T;
    message_handle     RAW(16);
    msg                SYS.AQ$_JMS_TEXT_MESSAGE;
BEGIN
    msg := SYS.AQ$_JMS_TEXT_MESSAGE.construct;
    msg.set_text('HELLO PLSQL WORLD ! ' || TO_CHAR(sysdate, 'DD-MM-YY HH24:MI:SS'));
    DBMS_AQ.ENQUEUE(
            queue_name => 'FRANK.EXAMPLE_QUEUE_1',
            enqueue_options => enqueue_options,
            message_properties => message_properties,
            payload => msg,
            msgid => message_handle);
    COMMIT;
END;
/
```

# Enqueue with DB trigger
```sql
CREATE TABLE FRANK.TEST (id NUMBER(5) PRIMARY KEY,val VARCHAR2(15) NOT NULL);


CREATE OR REPLACE TRIGGER ins_event_trigger 
   	BEFORE INSERT ON FRANK.TEST 
   	FOR EACH ROW
   
	DECLARE
    	enqueue_options    DBMS_AQ.ENQUEUE_OPTIONS_T;
    	message_properties DBMS_AQ.MESSAGE_PROPERTIES_T;
    	message_handle     RAW(16);
    	msg                SYS.AQ$_JMS_TEXT_MESSAGE;
	BEGIN
    	msg := SYS.AQ$_JMS_TEXT_MESSAGE.construct;
        msg.set_int_property('tab_id', :new.id);
    	msg.set_text(:new.val);
    	DBMS_AQ.ENQUEUE(
            queue_name => 'FRANK.EXAMPLE_QUEUE_1',
            enqueue_options => enqueue_options,
            message_properties => message_properties,
            payload => msg,
            msgid => message_handle);
END;
/

INSERT INTO FRANK.TEST(id, val) VALUES(1, 'test1');
INSERT INTO FRANK.TEST(id, val) VALUES(2, 'test2');
INSERT INTO FRANK.TEST(id, val) VALUES(3, 'test3');
INSERT INTO FRANK.TEST(id, val) VALUES(4, 'test4');
```

# helidon-aq-demo
Helidon AQ connector demo with OCI provisioned Oracle DB

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

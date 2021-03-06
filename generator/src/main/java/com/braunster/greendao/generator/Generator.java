package com.braunster.greendao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.ToOne;

/**
 * Created by itzik on 6/8/2014.
 */
public class Generator {

    // TODO set not null attribute to the properties that needs it.
    private static String outputDir = "../sdk/src/main/java";

    private static Entity user, linkedAccount, thread, message, threadUsers, linkedContact, follower;

    public static void main(String args[]) throws Exception{
//        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(54,"com.braunster.chatsdk.dao");

        schema.enableKeepSectionsByDefault();

        addUser(schema);
        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMessages(schema);
        addThread(schema);
        addThreadUsers(schema);
        addFollower(schema);

        setProperties();

//        setImplementation();
        setSuperClass();

        new DaoGenerator().generateAll(schema, outputDir);
    }

    //region Add Objects
    private static void addUser(Schema schema) {
        user = schema.addEntity(EntityProperties.BUser);
        user.addIdProperty();
        user.addStringProperty(EntityProperties.EntityID);
        user.addIntProperty(EntityProperties.AuthenticationType);
        user.addStringProperty(EntityProperties.MessageColor);
        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);
        user.addBooleanProperty(EntityProperties.Online);
        user.addStringProperty(EntityProperties.MetaData).columnName(EntityProperties.MetaData.toLowerCase());
    }

    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccount.addIdProperty();
        linkedAccount.addStringProperty(EntityProperties.Token);
        linkedAccount.addIntProperty(EntityProperties.Type);
    }

    private static void addLinkedContact(Schema schema) {
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addIdProperty();
        linkedContact.addStringProperty(EntityProperties.EntityID);
    }

    private static void addFollower(Schema schema) {
        follower = schema.addEntity(EntityProperties.BFollower);
        follower.addIdProperty();
        follower.addStringProperty(EntityProperties.EntityID);
        follower.addIntProperty(EntityProperties.Type);
    }

    private static void addThread(Schema schema) {
        thread = schema.addEntity(EntityProperties.BThread);
        thread.addIdProperty();
        thread.addStringProperty(EntityProperties.EntityID);
        thread.addDateProperty(EntityProperties.CreationDate);
        thread.addBooleanProperty(EntityProperties.HasUnreadMessaged);
        thread.addBooleanProperty(EntityProperties.BDeleted);
        thread.addStringProperty(EntityProperties.Name);
        thread.addDateProperty(EntityProperties.LastMessageAdded);
        thread.addIntProperty(EntityProperties.Type);
        thread.addStringProperty(EntityProperties.CreatorEntityID);
        thread.addStringProperty(EntityProperties.BThreadImageUrl);
        thread.addStringProperty(EntityProperties.RootKey).columnName(EntityProperties.C_RootKey);
        thread.addStringProperty(EntityProperties.ApiKey).columnName(EntityProperties.C_ApiKey);
    }

    private static void addThreadUsers(Schema schema){
        threadUsers = schema.addEntity(EntityProperties.BLinkData);
        threadUsers.addIdProperty().primaryKey();
    }

    private static void addMessages(Schema schema){
        message = schema.addEntity(EntityProperties.BMessage);
        message.addIdProperty();
        message.addStringProperty(EntityProperties.EntityID);
        message.addDateProperty(EntityProperties.Date);
        message.addBooleanProperty(EntityProperties.isRead);
        message.addStringProperty(EntityProperties.Resource);
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addStringProperty(EntityProperties.ImageDimensions);
        message.addIntProperty(EntityProperties.Type);
        message.addIntProperty(EntityProperties.Status);
        message.addIntProperty(EntityProperties.Delivered);
    }
    //endregion

    private static void setProperties(){
        Property userPropOwner = follower.addLongProperty(EntityProperties.OwnerId).getProperty();
        ToOne toOneUserPropOwner = follower.addToOne(user, userPropOwner);
        toOneUserPropOwner.setName(EntityProperties.Owner);

        Property userPropUser = follower.addLongProperty(EntityProperties.BUserId).getProperty();
        ToOne toOneUserPropUser = follower.addToOne(user, userPropUser);
        toOneUserPropUser.setName(EntityProperties.User);

        // LinkedContact, LinkedAccount and MetaData - START
        Property userProp = linkedContact.addLongProperty(EntityProperties.Owner).getProperty();
        ToOne toOneUserProp = linkedContact.addToOne(user, userProp);
        toOneUserProp.setName("Contact");

        Property userProp2 = linkedAccount.addLongProperty(EntityProperties.User).getProperty();
        linkedAccount.addToOne(user, userProp2);

        // Add a thread owner to the message
        Property threadIDProp = message.addLongProperty("OwnerThread").getProperty();
        ToOne one1 = message.addToOne(thread, threadIDProp);
        one1.setName("BThreadOwner");

        // The sender ID
        Property senderIDProp = message.addLongProperty("Sender").getProperty();
        ToOne one = message.addToOne(user, senderIDProp);
        one.setName("BUserSender");

        // Link data for user and thread.
        Property userIdProp = threadUsers.addLongProperty("UserID").getProperty();
        Property threadIdProp = threadUsers.addLongProperty("ThreadID").getProperty();
        threadUsers.addToOne(user, userIdProp);
        threadUsers.addToOne(thread, threadIdProp);
        //LinkedContact, LinkedAccount and MetaData - END

        // Threads - START
        Property creatorProp = thread.addLongProperty(EntityProperties.CreatorID).getProperty();
        ToOne one2 = thread.addToOne(user, creatorProp);
        one2.setName(EntityProperties.Creator);

        ToMany messagesProp = thread.addToMany(message, threadIDProp);
        messagesProp.setName(EntityProperties.Messages);

        ToMany linkToUsers = thread.addToMany(threadUsers, threadIdProp);
        linkToUsers.setName(EntityProperties.BLinkData);
        // Threads - END

//      // Users - START
        ToMany contacts = user.addToMany(linkedContact, userProp);
        contacts.setName(EntityProperties.BLinkedContacts);

        ToMany followers = user.addToMany(follower, userProp);
        followers.setName(EntityProperties.BFollowers);

        ToMany accounts = user.addToMany(linkedAccount, userProp2);
        accounts.setName(EntityProperties.BLinkedAccounts);

        ToMany messagesForUser = user.addToMany(message, senderIDProp);
        messagesForUser.setName(EntityProperties.Messages);

        ToMany threadsCreatedForUser = user.addToMany(thread, creatorProp);
        threadsCreatedForUser.setName(EntityProperties.ThreadsCreated);

        ToMany linkToThread = user.addToMany(threadUsers, userIdProp);
        linkToThread.setName(EntityProperties.BLinkData);
//        // Users - END

    }

    private static void setKeepSection(){
        user.setHasKeepSections(true);
    }

    private static void setSuperClass(){
        user.setSuperclass("BUserEntity");
        message.setSuperclass("BMessageEntity");
        thread.setSuperclass("BThreadEntity");
        linkedAccount.setSuperclass("BLinkedAccountEntity");
        linkedContact.setSuperclass("Entity");
        threadUsers.setSuperclass("Entity");
        follower.setSuperclass("BFollowerEntity");
    }
}

package io.mosaicboot.util.mongodb

import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionStatus

class DummyMongoTransactionManager(
    databaseFactory: MongoDatabaseFactory
) : MongoTransactionManager(databaseFactory) {
    override fun doGetTransaction(): Any {
        return Object()
    }

    override fun doBegin(transaction: Any, definition: TransactionDefinition) {
    }

    override fun doCommit(transactionObject: MongoTransactionObject) {
    }

    override fun doRollback(status: DefaultTransactionStatus) {
    }
}
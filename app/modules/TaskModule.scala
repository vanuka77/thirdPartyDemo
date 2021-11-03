package modules

import play.api.inject.{SimpleModule, _}
import utils.silhouette.authenticator.task.AutoDeletingAuthenticatorsTask
/** Using for binding scheduled asynchronous tasks.*/
class TaskModule extends SimpleModule(bind[AutoDeletingAuthenticatorsTask].toSelf.eagerly())
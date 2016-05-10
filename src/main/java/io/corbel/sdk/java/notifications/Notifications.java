package io.corbel.sdk.java.notifications;

import io.corbel.sdk.error.ApiError;
import scala.Unit;
import scala.util.Either;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Created by ismael on 5/05/16.
 */
public interface Notifications {

    CompletionStage<Either<ApiError, Unit>> sendNotification(String id, String recipient, Map<String, String> properties);
}

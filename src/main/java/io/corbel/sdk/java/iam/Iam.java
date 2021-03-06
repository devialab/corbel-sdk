package io.corbel.sdk.java.iam;

import io.corbel.sdk.api.RequestParams;
import io.corbel.sdk.error.ApiError;
import io.corbel.sdk.iam.*;
import scala.Unit;
import scala.util.Either;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexander De Leon (alex.deleon@devialab.com)
 */
public interface Iam {

    /* ----------------- Authentication ------------ */

    CompletionStage<Either<ApiError, AuthenticationResponse>> authenticate(ClientCredentials clientCredentials,
                                                                           Optional<UserCredentials> userCredentials,
                                                                           AuthenticationOptions authenticationOptions);

    CompletionStage<Either<ApiError, AuthenticationResponse>> authenticationRefresh(ClientCredentials clientCredentials,
                                                                           String refreshToken,
                                                                           AuthenticationOptions authenticationOptions);

  /* ----------------- Scopes ---------------------- */

    CompletionStage<Either<ApiError, Scope>> getScope(String id);


  /* ----------------- Users ---------------------- */

    CompletionStage<Either<ApiError, String>> createUser(User user);

    CompletionStage<Either<ApiError, User>> getUserById(String id);

    CompletionStage<Either<ApiError, User>> getUserIdByUsername(String username);

    CompletionStage<Either<ApiError, User>> getUser();

    CompletionStage<Either<ApiError, Unit>> updateUser(User user);

    CompletionStage<Either<ApiError, Collection<Device>>> getUserDevices(String userId);

    CompletionStage<Either<ApiError, Unit>> addGroupsToUser(String userId, Collection<String> groups);

    CompletionStage<Either<ApiError, Unit>> deleteGroupToUser(String userId, String groupId);

    CompletionStage<Either<ApiError, Collection<User>>> findUsers(RequestParams params);


  /* ----------------- Groups ---------------------- */

    CompletionStage<Either<ApiError, String>> createGroup(Group group);

}

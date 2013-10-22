package org.springframework.ldap.repository.support;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.StringPath;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QPerson is a Querydsl query type for Person
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPerson extends EntityPathBase<UnitTestPerson> {

    private static final long serialVersionUID = -1526737794;

    public static final QPerson person = new QPerson("person");

    public final StringPath fullName = createString("fullName");

    public final ListPath<String, StringPath> description = this.<String, StringPath>createList("description", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath lastName = createString("lastName");

    public QPerson(String variable) {
        super(UnitTestPerson.class, forVariable(variable));
    }

    public QPerson(Path<? extends UnitTestPerson> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPerson(PathMetadata<?> metadata) {
        super(UnitTestPerson.class, metadata);
    }

}


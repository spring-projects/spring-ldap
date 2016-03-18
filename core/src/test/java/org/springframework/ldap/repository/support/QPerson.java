package org.springframework.ldap.repository.support;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.ldap.odm.core.impl.UnitTestPerson;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


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

    public QPerson(PathMetadata metadata) {
        super(UnitTestPerson.class, metadata);
    }

}


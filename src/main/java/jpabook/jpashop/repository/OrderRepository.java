package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor //final을 생성자 만들어줌
public class OrderRepository {

    private final EntityManager em;

    /**
     * 주문 저장
     */
    public void save(Order order) {
        em.persist(order);
    }

    /**
     *단건 조회
     */
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     *검색 로직
     */
    public List<Order> findAll(OrderSearch orderSearch) {

//        return em.createQuery("select o from Order o join o.member m" +
////                " where o.status = :status" +
////                " and m.name like :name", Order.class)
////                .setParameter("status", orderSearch.getOrderStatus())
////                .setParameter("name", orderSearch.getMemberName())
//////                .setFirstResult(100) //페이징 할때 시작점
////                .setMaxResults(1000) //최대 1000건
////                .getResultList();

        //동적쿼리

        //JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있다.
        String jpql = "select o from Order o join o.member m"; //Order와 연관된 member 조인
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

        /**
         * JPA Criteria 유지보수성 x
         */
        public List<Order> findAllByCriteria(OrderSearch orderSearch) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> o = cq.from(Order.class);
            Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
            List<Predicate> criteria = new ArrayList<>();
            //주문 상태 검색
            if (orderSearch.getOrderStatus() != null) {
                Predicate status = cb.equal(o.get("status"),
                        orderSearch.getOrderStatus());
                criteria.add(status);
            }
            //회원 이름 검색
            if (StringUtils.hasText(orderSearch.getMemberName())) {
                Predicate name =
                        cb.like(m.<String>get("name"), "%" +
                                orderSearch.getMemberName() + "%");
                criteria.add(name);
            }
            cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
            TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
            return query.getResultList();
        }

}

--
-- PostgreSQL database dump
--

\restrict TMxt5rFxams6CHRCfeGaf2L1d7cSrqEpFhuGdK6BIJfW47kDaxZ3yxUyHGsm4LZ

-- Dumped from database version 17.10 (9f6157c)
-- Dumped by pg_dump version 18.3

-- Started on 2026-06-25 23:31:05

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 24576)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- TOC entry 4034 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- TOC entry 3 (class 3079 OID 24657)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 4035 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 1065 (class 1247 OID 49153)
-- Name: checkout_session_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkout_session_status AS ENUM (
    'creating',
    'reserved',
    'completed',
    'failed',
    'expired',
    'released'
);


--
-- TOC entry 1098 (class 1247 OID 65800)
-- Name: checkoutsessionstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkoutsessionstatus AS ENUM (
    'completed',
    'creating',
    'expired',
    'failed',
    'released',
    'reserved'
);


--
-- TOC entry 990 (class 1247 OID 24738)
-- Name: discount_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.discount_type AS ENUM (
    'percentage',
    'fixed_amount'
);


--
-- TOC entry 1119 (class 1247 OID 65892)
-- Name: discounttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.discounttype AS ENUM (
    'fixed_amount',
    'percentage'
);


--
-- TOC entry 978 (class 1247 OID 24700)
-- Name: gender_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.gender_type AS ENUM (
    'male',
    'female',
    'other'
);


--
-- TOC entry 1116 (class 1247 OID 65882)
-- Name: gendertype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.gendertype AS ENUM (
    'female',
    'male',
    'other'
);


--
-- TOC entry 993 (class 1247 OID 24744)
-- Name: image_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.image_type AS ENUM (
    'main',
    'thumbnail',
    'gallery'
);


--
-- TOC entry 1092 (class 1247 OID 65549)
-- Name: imagetype; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.imagetype AS public.image_type;


--
-- TOC entry 981 (class 1247 OID 24708)
-- Name: order_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.order_status AS ENUM (
    'pending',
    'processing',
    'shipping',
    'completed',
    'cancelled'
);


--
-- TOC entry 1104 (class 1247 OID 65828)
-- Name: orderstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.orderstatus AS ENUM (
    'cancelled',
    'completed',
    'pending',
    'processing',
    'shipping'
);


--
-- TOC entry 1071 (class 1247 OID 49176)
-- Name: payment_attempt_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_attempt_status AS ENUM (
    'pending',
    'completed',
    'failed',
    'expired',
    'requires_refund',
    'refund_requested',
    'refunded'
);


--
-- TOC entry 984 (class 1247 OID 24720)
-- Name: payment_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_method AS ENUM (
    'cod',
    'vnpay',
    'momo'
);


--
-- TOC entry 987 (class 1247 OID 24728)
-- Name: payment_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_status AS ENUM (
    'pending',
    'completed',
    'failed',
    'refunded'
);


--
-- TOC entry 1113 (class 1247 OID 65864)
-- Name: paymentattemptstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymentattemptstatus AS ENUM (
    'completed',
    'expired',
    'failed',
    'pending',
    'refund_requested',
    'refunded',
    'requires_refund'
);


--
-- TOC entry 1095 (class 1247 OID 65790)
-- Name: paymentmethod; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymentmethod AS ENUM (
    'cod',
    'momo',
    'vnpay'
);


--
-- TOC entry 1110 (class 1247 OID 65852)
-- Name: paymentstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymentstatus AS ENUM (
    'completed',
    'failed',
    'pending',
    'refunded'
);


--
-- TOC entry 1068 (class 1247 OID 49166)
-- Name: reservation_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.reservation_status AS ENUM (
    'active',
    'consumed',
    'released',
    'expired'
);


--
-- TOC entry 1101 (class 1247 OID 65816)
-- Name: reservationstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.reservationstatus AS ENUM (
    'active',
    'consumed',
    'expired',
    'released'
);


--
-- TOC entry 975 (class 1247 OID 24695)
-- Name: user_role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_role AS ENUM (
    'admin',
    'customer',
    'staff'
);


--
-- TOC entry 1107 (class 1247 OID 65842)
-- Name: userrole; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.userrole AS ENUM (
    'admin',
    'customer',
    'staff'
);


--
-- TOC entry 3506 (class 2605 OID 65814)
-- Name: CAST (public.checkoutsessionstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.checkoutsessionstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3513 (class 2605 OID 65898)
-- Name: CAST (public.discounttype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.discounttype AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3512 (class 2605 OID 65890)
-- Name: CAST (public.gendertype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.gendertype AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3508 (class 2605 OID 65840)
-- Name: CAST (public.orderstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.orderstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3511 (class 2605 OID 65880)
-- Name: CAST (public.paymentattemptstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.paymentattemptstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3505 (class 2605 OID 65798)
-- Name: CAST (public.paymentmethod AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.paymentmethod AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3510 (class 2605 OID 65862)
-- Name: CAST (public.paymentstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.paymentstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3507 (class 2605 OID 65826)
-- Name: CAST (public.reservationstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.reservationstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3509 (class 2605 OID 65850)
-- Name: CAST (public.userrole AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.userrole AS character varying) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3418 (class 2605 OID 65813)
-- Name: CAST (character varying AS public.checkoutsessionstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.checkoutsessionstatus) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3425 (class 2605 OID 65897)
-- Name: CAST (character varying AS public.discounttype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.discounttype) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3424 (class 2605 OID 65889)
-- Name: CAST (character varying AS public.gendertype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.gendertype) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3420 (class 2605 OID 65839)
-- Name: CAST (character varying AS public.orderstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.orderstatus) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3423 (class 2605 OID 65879)
-- Name: CAST (character varying AS public.paymentattemptstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.paymentattemptstatus) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3417 (class 2605 OID 65797)
-- Name: CAST (character varying AS public.paymentmethod); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.paymentmethod) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3422 (class 2605 OID 65861)
-- Name: CAST (character varying AS public.paymentstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.paymentstatus) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3419 (class 2605 OID 65825)
-- Name: CAST (character varying AS public.reservationstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.reservationstatus) WITH INOUT AS IMPLICIT;


--
-- TOC entry 3421 (class 2605 OID 65849)
-- Name: CAST (character varying AS public.userrole); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.userrole) WITH INOUT AS IMPLICIT;


--
-- TOC entry 278 (class 1255 OID 25238)
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 264 (class 1259 OID 25190)
-- Name: activity_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.activity_logs (
    id bigint NOT NULL,
    user_id bigint,
    action character varying(50) NOT NULL,
    entity_type character varying(50),
    entity_id bigint,
    old_data jsonb,
    new_data jsonb,
    ip_address inet,
    user_agent text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4036 (class 0 OID 0)
-- Dependencies: 264
-- Name: TABLE activity_logs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.activity_logs IS 'Log hoạt động (yêu cầu hệ thống #9): đăng nhập, thay đổi dữ liệu, xử lý đơn hàng.';


--
-- TOC entry 263 (class 1259 OID 25189)
-- Name: activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.activity_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4037 (class 0 OID 0)
-- Dependencies: 263
-- Name: activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.activity_logs_id_seq OWNED BY public.activity_logs.id;


--
-- TOC entry 224 (class 1259 OID 24794)
-- Name: addresses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.addresses (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    recipient_name character varying(100) NOT NULL,
    phone character varying(15) NOT NULL,
    province character varying(100) NOT NULL,
    district character varying(100) NOT NULL,
    ward character varying(100) NOT NULL,
    street_address character varying(255) NOT NULL,
    is_default boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4038 (class 0 OID 0)
-- Dependencies: 224
-- Name: TABLE addresses; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.addresses IS 'Địa chỉ giao hàng. Mỗi KH có nhiều địa chỉ, 1 địa chỉ mặc định.';


--
-- TOC entry 223 (class 1259 OID 24793)
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4039 (class 0 OID 0)
-- Dependencies: 223
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.addresses_id_seq OWNED BY public.addresses.id;


--
-- TOC entry 254 (class 1259 OID 25105)
-- Name: banners; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.banners (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    image_url text NOT NULL,
    link_url text,
    display_order integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    start_date timestamp with time zone,
    end_date timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4040 (class 0 OID 0)
-- Dependencies: 254
-- Name: TABLE banners; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.banners IS 'Banner/slider trang chủ. Admin quản lý thứ tự, thời gian hiển thị.';


--
-- TOC entry 253 (class 1259 OID 25104)
-- Name: banners_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.banners_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4041 (class 0 OID 0)
-- Dependencies: 253
-- Name: banners_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.banners_id_seq OWNED BY public.banners.id;


--
-- TOC entry 256 (class 1259 OID 25118)
-- Name: blog_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blog_categories (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    slug character varying(100) NOT NULL,
    description text,
    display_order integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4042 (class 0 OID 0)
-- Dependencies: 256
-- Name: TABLE blog_categories; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.blog_categories IS 'Danh mục blog: Xu hướng thời trang, Hướng dẫn phối đồ, Tin tức.';


--
-- TOC entry 255 (class 1259 OID 25117)
-- Name: blog_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blog_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4043 (class 0 OID 0)
-- Dependencies: 255
-- Name: blog_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.blog_categories_id_seq OWNED BY public.blog_categories.id;


--
-- TOC entry 262 (class 1259 OID 25171)
-- Name: blog_post_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blog_post_tags (
    id bigint NOT NULL,
    blog_post_id bigint NOT NULL,
    blog_tag_id bigint NOT NULL
);


--
-- TOC entry 261 (class 1259 OID 25170)
-- Name: blog_post_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blog_post_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4044 (class 0 OID 0)
-- Dependencies: 261
-- Name: blog_post_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.blog_post_tags_id_seq OWNED BY public.blog_post_tags.id;


--
-- TOC entry 258 (class 1259 OID 25135)
-- Name: blog_posts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blog_posts (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    content text NOT NULL,
    excerpt text,
    thumbnail_url text,
    category_id bigint,
    author_id bigint NOT NULL,
    is_published boolean DEFAULT false NOT NULL,
    published_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4045 (class 0 OID 0)
-- Dependencies: 258
-- Name: TABLE blog_posts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.blog_posts IS 'Bài viết blog/tin tức thời trang.';


--
-- TOC entry 257 (class 1259 OID 25134)
-- Name: blog_posts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blog_posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4046 (class 0 OID 0)
-- Dependencies: 257
-- Name: blog_posts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.blog_posts_id_seq OWNED BY public.blog_posts.id;


--
-- TOC entry 260 (class 1259 OID 25159)
-- Name: blog_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blog_tags (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    slug character varying(50) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4047 (class 0 OID 0)
-- Dependencies: 260
-- Name: TABLE blog_tags; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.blog_tags IS 'Tags cho blog: áo polo, mùa hè, streetwear, v.v.';


--
-- TOC entry 259 (class 1259 OID 25158)
-- Name: blog_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blog_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4048 (class 0 OID 0)
-- Dependencies: 259
-- Name: blog_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.blog_tags_id_seq OWNED BY public.blog_tags.id;


--
-- TOC entry 252 (class 1259 OID 25082)
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    product_variant_id bigint NOT NULL,
    quantity integer DEFAULT 1 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT cart_items_quantity_check CHECK ((quantity > 0))
);


--
-- TOC entry 4049 (class 0 OID 0)
-- Dependencies: 252
-- Name: TABLE cart_items; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cart_items IS 'Giỏ hàng KH đã đăng nhập. Khách vãng lai dùng Redis (session-based).';


--
-- TOC entry 251 (class 1259 OID 25081)
-- Name: cart_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cart_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4050 (class 0 OID 0)
-- Dependencies: 251
-- Name: cart_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cart_items_id_seq OWNED BY public.cart_items.id;


--
-- TOC entry 226 (class 1259 OID 24811)
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    slug character varying(100) NOT NULL,
    parent_id bigint,
    description text,
    display_order integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4051 (class 0 OID 0)
-- Dependencies: 226
-- Name: TABLE categories; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.categories IS 'Danh mục sản phẩm đa cấp (QĐ5: tối đa 3 cấp). VD: Nam > Áo > Áo Polo.';


--
-- TOC entry 225 (class 1259 OID 24810)
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4052 (class 0 OID 0)
-- Dependencies: 225
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- TOC entry 268 (class 1259 OID 49222)
-- Name: checkout_session_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkout_session_items (
    id bigint NOT NULL,
    checkout_session_id bigint NOT NULL,
    product_variant_id bigint NOT NULL,
    product_name character varying(255) NOT NULL,
    variant_info character varying(100) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT checkout_session_items_quantity_check CHECK ((quantity > 0)),
    CONSTRAINT checkout_session_items_subtotal_check CHECK ((subtotal >= (0)::numeric)),
    CONSTRAINT checkout_session_items_unit_price_check CHECK ((unit_price >= (0)::numeric))
);


--
-- TOC entry 4053 (class 0 OID 0)
-- Dependencies: 268
-- Name: TABLE checkout_session_items; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.checkout_session_items IS 'Snapshot san pham tai thoi diem checkout.';


--
-- TOC entry 267 (class 1259 OID 49221)
-- Name: checkout_session_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkout_session_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4054 (class 0 OID 0)
-- Dependencies: 267
-- Name: checkout_session_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkout_session_items_id_seq OWNED BY public.checkout_session_items.id;


--
-- TOC entry 266 (class 1259 OID 49192)
-- Name: checkout_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkout_sessions (
    id bigint NOT NULL,
    checkout_code character varying(30) NOT NULL,
    user_id bigint NOT NULL,
    shipping_name character varying(100) NOT NULL,
    shipping_phone character varying(15) NOT NULL,
    shipping_province character varying(100) NOT NULL,
    shipping_district character varying(100) NOT NULL,
    shipping_ward character varying(100) NOT NULL,
    shipping_address character varying(255) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    shipping_fee numeric(12,2) DEFAULT 0 NOT NULL,
    discount_amount numeric(12,2) DEFAULT 0 NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    voucher_id bigint,
    payment_method public.payment_method NOT NULL,
    status public.checkout_session_status DEFAULT 'creating'::public.checkout_session_status NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT checkout_sessions_discount_amount_check CHECK ((discount_amount >= (0)::numeric)),
    CONSTRAINT checkout_sessions_shipping_fee_check CHECK ((shipping_fee >= (0)::numeric)),
    CONSTRAINT checkout_sessions_subtotal_check CHECK ((subtotal >= (0)::numeric)),
    CONSTRAINT checkout_sessions_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


--
-- TOC entry 4055 (class 0 OID 0)
-- Dependencies: 266
-- Name: TABLE checkout_sessions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.checkout_sessions IS 'Checkout session luu snapshot gio hang va dia chi truoc khi tao don hang.';


--
-- TOC entry 265 (class 1259 OID 49191)
-- Name: checkout_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkout_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4056 (class 0 OID 0)
-- Dependencies: 265
-- Name: checkout_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkout_sessions_id_seq OWNED BY public.checkout_sessions.id;


--
-- TOC entry 236 (class 1259 OID 24910)
-- Name: collection_products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.collection_products (
    id bigint NOT NULL,
    collection_id bigint NOT NULL,
    product_id bigint NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 235 (class 1259 OID 24909)
-- Name: collection_products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.collection_products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4057 (class 0 OID 0)
-- Dependencies: 235
-- Name: collection_products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.collection_products_id_seq OWNED BY public.collection_products.id;


--
-- TOC entry 234 (class 1259 OID 24896)
-- Name: collections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.collections (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    description text,
    banner_url text,
    start_date timestamp with time zone,
    end_date timestamp with time zone,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4058 (class 0 OID 0)
-- Dependencies: 234
-- Name: TABLE collections; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.collections IS 'Bộ sưu tập sản phẩm theo mùa/chủ đề.';


--
-- TOC entry 233 (class 1259 OID 24895)
-- Name: collections_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.collections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4059 (class 0 OID 0)
-- Dependencies: 233
-- Name: collections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.collections_id_seq OWNED BY public.collections.id;


--
-- TOC entry 270 (class 1259 OID 49243)
-- Name: inventory_reservations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory_reservations (
    id bigint NOT NULL,
    checkout_session_id bigint NOT NULL,
    product_variant_id bigint NOT NULL,
    quantity integer NOT NULL,
    status public.reservation_status DEFAULT 'active'::public.reservation_status NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT inventory_reservations_quantity_check CHECK ((quantity > 0))
);


--
-- TOC entry 4060 (class 0 OID 0)
-- Dependencies: 270
-- Name: TABLE inventory_reservations; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.inventory_reservations IS 'Giu ton kho tam thoi cho checkout, chua tru stock_quantity khi reserve.';


--
-- TOC entry 269 (class 1259 OID 49242)
-- Name: inventory_reservations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inventory_reservations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4061 (class 0 OID 0)
-- Dependencies: 269
-- Name: inventory_reservations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inventory_reservations_id_seq OWNED BY public.inventory_reservations.id;


--
-- TOC entry 220 (class 1259 OID 24752)
-- Name: membership_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.membership_tiers (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    slug character varying(50) NOT NULL,
    min_points integer DEFAULT 0 NOT NULL,
    discount_percent numeric(5,2) DEFAULT 0 NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT membership_tiers_discount_percent_check CHECK (((discount_percent >= (0)::numeric) AND (discount_percent <= (100)::numeric)))
);


--
-- TOC entry 4062 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE membership_tiers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.membership_tiers IS 'Hạng thành viên: Đồng, Bạc, Vàng, Kim cương. Admin có thể sửa qua CMS.';


--
-- TOC entry 219 (class 1259 OID 24751)
-- Name: membership_tiers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.membership_tiers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4063 (class 0 OID 0)
-- Dependencies: 219
-- Name: membership_tiers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.membership_tiers_id_seq OWNED BY public.membership_tiers.id;


--
-- TOC entry 242 (class 1259 OID 24978)
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_variant_id bigint NOT NULL,
    product_name character varying(255) NOT NULL,
    variant_info character varying(100) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    CONSTRAINT order_items_quantity_check CHECK ((quantity > 0)),
    CONSTRAINT order_items_subtotal_check CHECK ((subtotal >= (0)::numeric)),
    CONSTRAINT order_items_unit_price_check CHECK ((unit_price >= (0)::numeric))
);


--
-- TOC entry 4064 (class 0 OID 0)
-- Dependencies: 242
-- Name: TABLE order_items; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.order_items IS 'Chi tiết đơn hàng. Snapshot giá/tên SP tại thời điểm mua (QĐ3).';


--
-- TOC entry 241 (class 1259 OID 24977)
-- Name: order_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4065 (class 0 OID 0)
-- Dependencies: 241
-- Name: order_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.order_items_id_seq OWNED BY public.order_items.id;


--
-- TOC entry 276 (class 1259 OID 57345)
-- Name: order_status_histories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_status_histories (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    from_status public.order_status,
    to_status public.order_status NOT NULL,
    changed_by bigint,
    changed_by_role public.user_role,
    reason text,
    metadata jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4066 (class 0 OID 0)
-- Dependencies: 276
-- Name: TABLE order_status_histories; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.order_status_histories IS 'Timeline trang thai don hang phuc vu audit va Staff order detail.';


--
-- TOC entry 275 (class 1259 OID 57344)
-- Name: order_status_histories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_status_histories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4067 (class 0 OID 0)
-- Dependencies: 275
-- Name: order_status_histories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.order_status_histories_id_seq OWNED BY public.order_status_histories.id;


--
-- TOC entry 240 (class 1259 OID 24948)
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    order_code character varying(20) NOT NULL,
    shipping_name character varying(100) NOT NULL,
    shipping_phone character varying(15) NOT NULL,
    shipping_province character varying(100) NOT NULL,
    shipping_district character varying(100) NOT NULL,
    shipping_ward character varying(100) NOT NULL,
    shipping_address character varying(255) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    shipping_fee numeric(12,2) DEFAULT 0 NOT NULL,
    discount_amount numeric(12,2) DEFAULT 0 NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    voucher_id bigint,
    status public.order_status DEFAULT 'pending'::public.order_status NOT NULL,
    note text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT orders_discount_amount_check CHECK ((discount_amount >= (0)::numeric)),
    CONSTRAINT orders_shipping_fee_check CHECK ((shipping_fee >= (0)::numeric)),
    CONSTRAINT orders_subtotal_check CHECK ((subtotal >= (0)::numeric)),
    CONSTRAINT orders_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


--
-- TOC entry 4068 (class 0 OID 0)
-- Dependencies: 240
-- Name: TABLE orders; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.orders IS 'Đơn hàng (QĐ8). Ship: đơn < 500K → 30K, đơn >= 500K → miễn phí (app logic).';


--
-- TOC entry 239 (class 1259 OID 24947)
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4069 (class 0 OID 0)
-- Dependencies: 239
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- TOC entry 274 (class 1259 OID 49290)
-- Name: payment_attempts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_attempts (
    id bigint NOT NULL,
    payment_reference character varying(50) NOT NULL,
    checkout_session_id bigint NOT NULL,
    method public.payment_method NOT NULL,
    amount numeric(12,2) NOT NULL,
    status public.payment_attempt_status DEFAULT 'pending'::public.payment_attempt_status NOT NULL,
    payment_url text,
    gateway_transaction_id character varying(255),
    gateway_payload jsonb,
    failure_reason text,
    requires_refund_reason text,
    expires_at timestamp with time zone NOT NULL,
    completed_at timestamp with time zone,
    failed_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT payment_attempts_amount_check CHECK ((amount >= (0)::numeric))
);


--
-- TOC entry 4070 (class 0 OID 0)
-- Dependencies: 274
-- Name: TABLE payment_attempts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.payment_attempts IS 'Lan thu thanh toan online gan voi checkout session truoc khi co order.';


--
-- TOC entry 273 (class 1259 OID 49289)
-- Name: payment_attempts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_attempts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4071 (class 0 OID 0)
-- Dependencies: 273
-- Name: payment_attempts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_attempts_id_seq OWNED BY public.payment_attempts.id;


--
-- TOC entry 244 (class 1259 OID 24998)
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    method public.payment_method NOT NULL,
    amount numeric(12,2) NOT NULL,
    status public.payment_status DEFAULT 'pending'::public.payment_status NOT NULL,
    transaction_id character varying(255),
    payment_data jsonb,
    paid_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT payments_amount_check CHECK ((amount >= (0)::numeric))
);


--
-- TOC entry 4072 (class 0 OID 0)
-- Dependencies: 244
-- Name: TABLE payments; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.payments IS 'Thanh toán. Không lưu thông tin thẻ — delegate cho VNPay/MoMo.';


--
-- TOC entry 243 (class 1259 OID 24997)
-- Name: payments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4073 (class 0 OID 0)
-- Dependencies: 243
-- Name: payments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payments_id_seq OWNED BY public.payments.id;


--
-- TOC entry 232 (class 1259 OID 24879)
-- Name: product_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_images (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    image_url text NOT NULL,
    image_type public.image_type DEFAULT 'gallery'::public.image_type NOT NULL,
    display_order integer DEFAULT 0 NOT NULL,
    alt_text character varying(255),
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4074 (class 0 OID 0)
-- Dependencies: 232
-- Name: TABLE product_images; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.product_images IS 'Ảnh sản phẩm lưu trên S3. QĐ1: tối thiểu 1 ảnh main.';


--
-- TOC entry 231 (class 1259 OID 24878)
-- Name: product_images_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4075 (class 0 OID 0)
-- Dependencies: 231
-- Name: product_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_images_id_seq OWNED BY public.product_images.id;


--
-- TOC entry 230 (class 1259 OID 24857)
-- Name: product_variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_variants (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    sku character varying(50) NOT NULL,
    size character varying(10) NOT NULL,
    color character varying(50) NOT NULL,
    stock_quantity integer DEFAULT 0 NOT NULL,
    additional_price numeric(12,2) DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT product_variants_stock_quantity_check CHECK ((stock_quantity >= 0))
);


--
-- TOC entry 4076 (class 0 OID 0)
-- Dependencies: 230
-- Name: TABLE product_variants; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.product_variants IS 'Biến thể sản phẩm (QĐ2: SKU duy nhất, tồn kho >= 0). QĐ6: auto giảm khi đặt hàng.';


--
-- TOC entry 229 (class 1259 OID 24856)
-- Name: product_variants_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_variants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4077 (class 0 OID 0)
-- Dependencies: 229
-- Name: product_variants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_variants_id_seq OWNED BY public.product_variants.id;


--
-- TOC entry 228 (class 1259 OID 24831)
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    description text,
    material character varying(255),
    care_instructions text,
    category_id bigint NOT NULL,
    base_price numeric(12,2) NOT NULL,
    sale_price numeric(12,2),
    is_active boolean DEFAULT true NOT NULL,
    is_featured boolean DEFAULT false NOT NULL,
    total_sold integer DEFAULT 0 NOT NULL,
    average_rating numeric(3,2) DEFAULT 0,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT products_average_rating_check CHECK (((average_rating >= (0)::numeric) AND (average_rating <= (5)::numeric))),
    CONSTRAINT products_base_price_check CHECK ((base_price >= (0)::numeric)),
    CONSTRAINT products_sale_price_check CHECK (((sale_price IS NULL) OR (sale_price >= (0)::numeric))),
    CONSTRAINT products_total_sold_check CHECK ((total_sold >= 0))
);


--
-- TOC entry 4078 (class 0 OID 0)
-- Dependencies: 228
-- Name: TABLE products; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.products IS 'Sản phẩm. QĐ1: tên không trùng cùng danh mục. QĐ4: xóa mềm nếu đã có đơn hàng.';


--
-- TOC entry 227 (class 1259 OID 24830)
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4079 (class 0 OID 0)
-- Dependencies: 227
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- TOC entry 248 (class 1259 OID 25046)
-- Name: review_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.review_images (
    id bigint NOT NULL,
    review_id bigint NOT NULL,
    image_url text NOT NULL,
    display_order integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4080 (class 0 OID 0)
-- Dependencies: 248
-- Name: TABLE review_images; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.review_images IS 'Ảnh đánh giá (QĐ13: tối đa 5 ảnh — enforce ở app logic).';


--
-- TOC entry 247 (class 1259 OID 25045)
-- Name: review_images_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.review_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4081 (class 0 OID 0)
-- Dependencies: 247
-- Name: review_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.review_images_id_seq OWNED BY public.review_images.id;


--
-- TOC entry 246 (class 1259 OID 25015)
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    product_id bigint NOT NULL,
    order_id bigint NOT NULL,
    rating smallint NOT NULL,
    content text NOT NULL,
    is_approved boolean DEFAULT false NOT NULL,
    admin_reply text,
    replied_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    delete_reason text,
    is_active boolean DEFAULT true NOT NULL,
    CONSTRAINT reviews_content_check CHECK ((length(content) >= 10)),
    CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


--
-- TOC entry 4082 (class 0 OID 0)
-- Dependencies: 246
-- Name: TABLE reviews; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.reviews IS 'Đánh giá sản phẩm (QĐ9, QĐ13). Chỉ KH đã mua + đơn hoàn thành mới được đánh giá.';


--
-- TOC entry 245 (class 1259 OID 25014)
-- Name: reviews_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reviews_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4083 (class 0 OID 0)
-- Dependencies: 245
-- Name: reviews_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reviews_id_seq OWNED BY public.reviews.id;


--
-- TOC entry 222 (class 1259 OID 24770)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255),
    full_name character varying(100) NOT NULL,
    phone character varying(15),
    gender public.gender_type,
    date_of_birth date,
    avatar_url text,
    role public.user_role DEFAULT 'customer'::public.user_role NOT NULL,
    loyalty_points integer DEFAULT 0 NOT NULL,
    membership_tier_id bigint,
    auth_provider character varying(20) DEFAULT 'email'::character varying,
    email_verified boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT users_loyalty_points_check CHECK ((loyalty_points >= 0))
);


--
-- TOC entry 4084 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.users IS 'Người dùng hệ thống: admin và khách hàng. QĐ14: không được sửa email. QĐ16: email unique.';


--
-- TOC entry 221 (class 1259 OID 24769)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4085 (class 0 OID 0)
-- Dependencies: 221
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 272 (class 1259 OID 49266)
-- Name: voucher_reservations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.voucher_reservations (
    id bigint NOT NULL,
    checkout_session_id bigint NOT NULL,
    voucher_id bigint NOT NULL,
    discount_amount numeric(12,2) DEFAULT 0 NOT NULL,
    status public.reservation_status DEFAULT 'active'::public.reservation_status NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT voucher_reservations_discount_amount_check CHECK ((discount_amount >= (0)::numeric))
);


--
-- TOC entry 4086 (class 0 OID 0)
-- Dependencies: 272
-- Name: TABLE voucher_reservations; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.voucher_reservations IS 'Giu luot su dung voucher tam thoi cho checkout, chua tang times_used khi reserve.';


--
-- TOC entry 271 (class 1259 OID 49265)
-- Name: voucher_reservations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.voucher_reservations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4087 (class 0 OID 0)
-- Dependencies: 271
-- Name: voucher_reservations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.voucher_reservations_id_seq OWNED BY public.voucher_reservations.id;


--
-- TOC entry 238 (class 1259 OID 24930)
-- Name: vouchers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vouchers (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    discount_type public.discount_type NOT NULL,
    discount_value numeric(12,2) NOT NULL,
    max_discount_amount numeric(12,2),
    min_order_amount numeric(12,2) DEFAULT 0 NOT NULL,
    start_date timestamp with time zone NOT NULL,
    end_date timestamp with time zone NOT NULL,
    usage_limit integer DEFAULT 1 NOT NULL,
    times_used integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT vouchers_check CHECK ((end_date > start_date)),
    CONSTRAINT vouchers_discount_value_check CHECK ((discount_value > (0)::numeric)),
    CONSTRAINT vouchers_times_used_check CHECK ((times_used >= 0))
);


--
-- TOC entry 4088 (class 0 OID 0)
-- Dependencies: 238
-- Name: TABLE vouchers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.vouchers IS 'Mã giảm giá (QĐ7). QĐ11: mỗi đơn chỉ 1 voucher, kiểm tra điều kiện.';


--
-- TOC entry 237 (class 1259 OID 24929)
-- Name: vouchers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.vouchers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4089 (class 0 OID 0)
-- Dependencies: 237
-- Name: vouchers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.vouchers_id_seq OWNED BY public.vouchers.id;


--
-- TOC entry 250 (class 1259 OID 25062)
-- Name: wishlists; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wishlists (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    product_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 4090 (class 0 OID 0)
-- Dependencies: 250
-- Name: TABLE wishlists; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.wishlists IS 'Danh sách sản phẩm yêu thích.';


--
-- TOC entry 249 (class 1259 OID 25061)
-- Name: wishlists_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.wishlists_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4091 (class 0 OID 0)
-- Dependencies: 249
-- Name: wishlists_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.wishlists_id_seq OWNED BY public.wishlists.id;


--
-- TOC entry 3607 (class 2604 OID 25193)
-- Name: activity_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs ALTER COLUMN id SET DEFAULT nextval('public.activity_logs_id_seq'::regclass);


--
-- TOC entry 3527 (class 2604 OID 24797)
-- Name: addresses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.addresses ALTER COLUMN id SET DEFAULT nextval('public.addresses_id_seq'::regclass);


--
-- TOC entry 3590 (class 2604 OID 25108)
-- Name: banners id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.banners ALTER COLUMN id SET DEFAULT nextval('public.banners_id_seq'::regclass);


--
-- TOC entry 3595 (class 2604 OID 25121)
-- Name: blog_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_categories ALTER COLUMN id SET DEFAULT nextval('public.blog_categories_id_seq'::regclass);


--
-- TOC entry 3606 (class 2604 OID 25174)
-- Name: blog_post_tags id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_post_tags ALTER COLUMN id SET DEFAULT nextval('public.blog_post_tags_id_seq'::regclass);


--
-- TOC entry 3600 (class 2604 OID 25138)
-- Name: blog_posts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_posts ALTER COLUMN id SET DEFAULT nextval('public.blog_posts_id_seq'::regclass);


--
-- TOC entry 3604 (class 2604 OID 25162)
-- Name: blog_tags id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_tags ALTER COLUMN id SET DEFAULT nextval('public.blog_tags_id_seq'::regclass);


--
-- TOC entry 3586 (class 2604 OID 25085)
-- Name: cart_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items ALTER COLUMN id SET DEFAULT nextval('public.cart_items_id_seq'::regclass);


--
-- TOC entry 3531 (class 2604 OID 24814)
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- TOC entry 3615 (class 2604 OID 49225)
-- Name: checkout_session_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_session_items ALTER COLUMN id SET DEFAULT nextval('public.checkout_session_items_id_seq'::regclass);


--
-- TOC entry 3609 (class 2604 OID 49195)
-- Name: checkout_sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_sessions ALTER COLUMN id SET DEFAULT nextval('public.checkout_sessions_id_seq'::regclass);


--
-- TOC entry 3557 (class 2604 OID 24913)
-- Name: collection_products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products ALTER COLUMN id SET DEFAULT nextval('public.collection_products_id_seq'::regclass);


--
-- TOC entry 3553 (class 2604 OID 24899)
-- Name: collections id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collections ALTER COLUMN id SET DEFAULT nextval('public.collections_id_seq'::regclass);


--
-- TOC entry 3617 (class 2604 OID 49246)
-- Name: inventory_reservations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations ALTER COLUMN id SET DEFAULT nextval('public.inventory_reservations_id_seq'::regclass);


--
-- TOC entry 3514 (class 2604 OID 24755)
-- Name: membership_tiers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_tiers ALTER COLUMN id SET DEFAULT nextval('public.membership_tiers_id_seq'::regclass);


--
-- TOC entry 3572 (class 2604 OID 24981)
-- Name: order_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items ALTER COLUMN id SET DEFAULT nextval('public.order_items_id_seq'::regclass);


--
-- TOC entry 3630 (class 2604 OID 57348)
-- Name: order_status_histories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_status_histories ALTER COLUMN id SET DEFAULT nextval('public.order_status_histories_id_seq'::regclass);


--
-- TOC entry 3566 (class 2604 OID 24951)
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- TOC entry 3626 (class 2604 OID 49293)
-- Name: payment_attempts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_attempts ALTER COLUMN id SET DEFAULT nextval('public.payment_attempts_id_seq'::regclass);


--
-- TOC entry 3573 (class 2604 OID 25001)
-- Name: payments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments ALTER COLUMN id SET DEFAULT nextval('public.payments_id_seq'::regclass);


--
-- TOC entry 3549 (class 2604 OID 24882)
-- Name: product_images id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images ALTER COLUMN id SET DEFAULT nextval('public.product_images_id_seq'::regclass);


--
-- TOC entry 3543 (class 2604 OID 24860)
-- Name: product_variants id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants ALTER COLUMN id SET DEFAULT nextval('public.product_variants_id_seq'::regclass);


--
-- TOC entry 3536 (class 2604 OID 24834)
-- Name: products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- TOC entry 3581 (class 2604 OID 25049)
-- Name: review_images id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_images ALTER COLUMN id SET DEFAULT nextval('public.review_images_id_seq'::regclass);


--
-- TOC entry 3576 (class 2604 OID 25018)
-- Name: reviews id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews ALTER COLUMN id SET DEFAULT nextval('public.reviews_id_seq'::regclass);


--
-- TOC entry 3519 (class 2604 OID 24773)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 3621 (class 2604 OID 49269)
-- Name: voucher_reservations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations ALTER COLUMN id SET DEFAULT nextval('public.voucher_reservations_id_seq'::regclass);


--
-- TOC entry 3559 (class 2604 OID 24933)
-- Name: vouchers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vouchers ALTER COLUMN id SET DEFAULT nextval('public.vouchers_id_seq'::regclass);


--
-- TOC entry 3584 (class 2604 OID 25065)
-- Name: wishlists id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists ALTER COLUMN id SET DEFAULT nextval('public.wishlists_id_seq'::regclass);


--
-- TOC entry 3788 (class 2606 OID 25198)
-- Name: activity_logs activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 3677 (class 2606 OID 24804)
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- TOC entry 3763 (class 2606 OID 25116)
-- Name: banners banners_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.banners
    ADD CONSTRAINT banners_pkey PRIMARY KEY (id);


--
-- TOC entry 3765 (class 2606 OID 25131)
-- Name: blog_categories blog_categories_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_categories
    ADD CONSTRAINT blog_categories_name_key UNIQUE (name);


--
-- TOC entry 3767 (class 2606 OID 25129)
-- Name: blog_categories blog_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_categories
    ADD CONSTRAINT blog_categories_pkey PRIMARY KEY (id);


--
-- TOC entry 3769 (class 2606 OID 25133)
-- Name: blog_categories blog_categories_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_categories
    ADD CONSTRAINT blog_categories_slug_key UNIQUE (slug);


--
-- TOC entry 3784 (class 2606 OID 25178)
-- Name: blog_post_tags blog_post_tags_blog_post_id_blog_tag_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_post_tags
    ADD CONSTRAINT blog_post_tags_blog_post_id_blog_tag_id_key UNIQUE (blog_post_id, blog_tag_id);


--
-- TOC entry 3786 (class 2606 OID 25176)
-- Name: blog_post_tags blog_post_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_post_tags
    ADD CONSTRAINT blog_post_tags_pkey PRIMARY KEY (id);


--
-- TOC entry 3771 (class 2606 OID 25145)
-- Name: blog_posts blog_posts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_pkey PRIMARY KEY (id);


--
-- TOC entry 3773 (class 2606 OID 25147)
-- Name: blog_posts blog_posts_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_slug_key UNIQUE (slug);


--
-- TOC entry 3778 (class 2606 OID 25167)
-- Name: blog_tags blog_tags_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_tags
    ADD CONSTRAINT blog_tags_name_key UNIQUE (name);


--
-- TOC entry 3780 (class 2606 OID 25165)
-- Name: blog_tags blog_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_tags
    ADD CONSTRAINT blog_tags_pkey PRIMARY KEY (id);


--
-- TOC entry 3782 (class 2606 OID 25169)
-- Name: blog_tags blog_tags_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_tags
    ADD CONSTRAINT blog_tags_slug_key UNIQUE (slug);


--
-- TOC entry 3756 (class 2606 OID 25091)
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- TOC entry 3758 (class 2606 OID 25093)
-- Name: cart_items cart_items_user_id_product_variant_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_user_id_product_variant_id_key UNIQUE (user_id, product_variant_id);


--
-- TOC entry 3680 (class 2606 OID 24822)
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- TOC entry 3682 (class 2606 OID 24824)
-- Name: categories categories_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_slug_key UNIQUE (slug);


--
-- TOC entry 3802 (class 2606 OID 49231)
-- Name: checkout_session_items checkout_session_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_session_items
    ADD CONSTRAINT checkout_session_items_pkey PRIMARY KEY (id);


--
-- TOC entry 3794 (class 2606 OID 49210)
-- Name: checkout_sessions checkout_sessions_checkout_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_sessions
    ADD CONSTRAINT checkout_sessions_checkout_code_key UNIQUE (checkout_code);


--
-- TOC entry 3796 (class 2606 OID 49208)
-- Name: checkout_sessions checkout_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_sessions
    ADD CONSTRAINT checkout_sessions_pkey PRIMARY KEY (id);


--
-- TOC entry 3713 (class 2606 OID 24918)
-- Name: collection_products collection_products_collection_id_product_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products
    ADD CONSTRAINT collection_products_collection_id_product_id_key UNIQUE (collection_id, product_id);


--
-- TOC entry 3715 (class 2606 OID 24916)
-- Name: collection_products collection_products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products
    ADD CONSTRAINT collection_products_pkey PRIMARY KEY (id);


--
-- TOC entry 3709 (class 2606 OID 24906)
-- Name: collections collections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_pkey PRIMARY KEY (id);


--
-- TOC entry 3711 (class 2606 OID 24908)
-- Name: collections collections_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_slug_key UNIQUE (slug);


--
-- TOC entry 3809 (class 2606 OID 49254)
-- Name: inventory_reservations inventory_reservations_checkout_session_id_product_variant__key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations
    ADD CONSTRAINT inventory_reservations_checkout_session_id_product_variant__key UNIQUE (checkout_session_id, product_variant_id);


--
-- TOC entry 3811 (class 2606 OID 49252)
-- Name: inventory_reservations inventory_reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations
    ADD CONSTRAINT inventory_reservations_pkey PRIMARY KEY (id);


--
-- TOC entry 3664 (class 2606 OID 24766)
-- Name: membership_tiers membership_tiers_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_tiers
    ADD CONSTRAINT membership_tiers_name_key UNIQUE (name);


--
-- TOC entry 3666 (class 2606 OID 24764)
-- Name: membership_tiers membership_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_tiers
    ADD CONSTRAINT membership_tiers_pkey PRIMARY KEY (id);


--
-- TOC entry 3668 (class 2606 OID 24768)
-- Name: membership_tiers membership_tiers_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_tiers
    ADD CONSTRAINT membership_tiers_slug_key UNIQUE (slug);


--
-- TOC entry 3733 (class 2606 OID 24986)
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- TOC entry 3832 (class 2606 OID 57353)
-- Name: order_status_histories order_status_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_status_histories
    ADD CONSTRAINT order_status_histories_pkey PRIMARY KEY (id);


--
-- TOC entry 3728 (class 2606 OID 24966)
-- Name: orders orders_order_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_code_key UNIQUE (order_code);


--
-- TOC entry 3730 (class 2606 OID 24964)
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- TOC entry 3827 (class 2606 OID 49303)
-- Name: payment_attempts payment_attempts_payment_reference_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_attempts
    ADD CONSTRAINT payment_attempts_payment_reference_key UNIQUE (payment_reference);


--
-- TOC entry 3829 (class 2606 OID 49301)
-- Name: payment_attempts payment_attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_attempts
    ADD CONSTRAINT payment_attempts_pkey PRIMARY KEY (id);


--
-- TOC entry 3737 (class 2606 OID 25008)
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- TOC entry 3707 (class 2606 OID 24889)
-- Name: product_images product_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_pkey PRIMARY KEY (id);


--
-- TOC entry 3698 (class 2606 OID 24868)
-- Name: product_variants product_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_pkey PRIMARY KEY (id);


--
-- TOC entry 3700 (class 2606 OID 24872)
-- Name: product_variants product_variants_product_id_size_color_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_product_id_size_color_key UNIQUE (product_id, size, color);


--
-- TOC entry 3702 (class 2606 OID 24870)
-- Name: product_variants product_variants_sku_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_sku_key UNIQUE (sku);


--
-- TOC entry 3691 (class 2606 OID 24848)
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- TOC entry 3693 (class 2606 OID 24850)
-- Name: products products_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_slug_key UNIQUE (slug);


--
-- TOC entry 3747 (class 2606 OID 25055)
-- Name: review_images review_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_images
    ADD CONSTRAINT review_images_pkey PRIMARY KEY (id);


--
-- TOC entry 3741 (class 2606 OID 25027)
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- TOC entry 3743 (class 2606 OID 25029)
-- Name: reviews reviews_user_id_product_id_order_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_user_id_product_id_order_id_key UNIQUE (user_id, product_id, order_id);


--
-- TOC entry 3818 (class 2606 OID 65676)
-- Name: voucher_reservations uk1o753bpa7nkbx7l10d9ql2o9u; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations
    ADD CONSTRAINT uk1o753bpa7nkbx7l10d9ql2o9u UNIQUE (checkout_session_id);


--
-- TOC entry 3717 (class 2606 OID 65668)
-- Name: collection_products uk6psije7ssv210emj6paxn0gvb; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products
    ADD CONSTRAINT uk6psije7ssv210emj6paxn0gvb UNIQUE (collection_id, product_id);


--
-- TOC entry 3761 (class 2606 OID 65666)
-- Name: cart_items uk9rtbtdw8mu686wmjo2qxc6xef; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT uk9rtbtdw8mu686wmjo2qxc6xef UNIQUE (user_id, product_variant_id);


--
-- TOC entry 3750 (class 2606 OID 65678)
-- Name: wishlists ukht6e6158srxsvjciahp1kjywf; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists
    ADD CONSTRAINT ukht6e6158srxsvjciahp1kjywf UNIQUE (user_id, product_id);


--
-- TOC entry 3813 (class 2606 OID 65670)
-- Name: inventory_reservations uknb7l4iu07yyo89kp054k9ol0h; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations
    ADD CONSTRAINT uknb7l4iu07yyo89kp054k9ol0h UNIQUE (checkout_session_id, product_variant_id);


--
-- TOC entry 3704 (class 2606 OID 65672)
-- Name: product_variants ukqftlp982upbw9ey6wuyt4a4ga; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT ukqftlp982upbw9ey6wuyt4a4ga UNIQUE (product_id, size, color);


--
-- TOC entry 3745 (class 2606 OID 65674)
-- Name: reviews ukr8vcm6neix10qorxt37o37kd8; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT ukr8vcm6neix10qorxt37o37kd8 UNIQUE (user_id, product_id, order_id);


--
-- TOC entry 3673 (class 2606 OID 24787)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3675 (class 2606 OID 24785)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3820 (class 2606 OID 49278)
-- Name: voucher_reservations voucher_reservations_checkout_session_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations
    ADD CONSTRAINT voucher_reservations_checkout_session_id_key UNIQUE (checkout_session_id);


--
-- TOC entry 3822 (class 2606 OID 49276)
-- Name: voucher_reservations voucher_reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations
    ADD CONSTRAINT voucher_reservations_pkey PRIMARY KEY (id);


--
-- TOC entry 3719 (class 2606 OID 24946)
-- Name: vouchers vouchers_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_code_key UNIQUE (code);


--
-- TOC entry 3721 (class 2606 OID 24944)
-- Name: vouchers vouchers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_pkey PRIMARY KEY (id);


--
-- TOC entry 3752 (class 2606 OID 25068)
-- Name: wishlists wishlists_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists
    ADD CONSTRAINT wishlists_pkey PRIMARY KEY (id);


--
-- TOC entry 3754 (class 2606 OID 25070)
-- Name: wishlists wishlists_user_id_product_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists
    ADD CONSTRAINT wishlists_user_id_product_id_key UNIQUE (user_id, product_id);


--
-- TOC entry 3678 (class 1259 OID 25207)
-- Name: idx_addresses_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_addresses_user ON public.addresses USING btree (user_id);


--
-- TOC entry 3774 (class 1259 OID 25231)
-- Name: idx_blog_posts_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_blog_posts_category ON public.blog_posts USING btree (category_id);


--
-- TOC entry 3775 (class 1259 OID 25232)
-- Name: idx_blog_posts_published; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_blog_posts_published ON public.blog_posts USING btree (is_published, published_at DESC);


--
-- TOC entry 3776 (class 1259 OID 25233)
-- Name: idx_blog_posts_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_blog_posts_slug ON public.blog_posts USING btree (slug);


--
-- TOC entry 3759 (class 1259 OID 25230)
-- Name: idx_cart_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_user ON public.cart_items USING btree (user_id);


--
-- TOC entry 3683 (class 1259 OID 25208)
-- Name: idx_categories_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_categories_parent ON public.categories USING btree (parent_id);


--
-- TOC entry 3684 (class 1259 OID 25209)
-- Name: idx_categories_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_categories_slug ON public.categories USING btree (slug);


--
-- TOC entry 3803 (class 1259 OID 49313)
-- Name: idx_checkout_session_items_checkout; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_session_items_checkout ON public.checkout_session_items USING btree (checkout_session_id);


--
-- TOC entry 3804 (class 1259 OID 49314)
-- Name: idx_checkout_session_items_variant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_session_items_variant ON public.checkout_session_items USING btree (product_variant_id);


--
-- TOC entry 3797 (class 1259 OID 49312)
-- Name: idx_checkout_sessions_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_sessions_code ON public.checkout_sessions USING btree (checkout_code);


--
-- TOC entry 3798 (class 1259 OID 49311)
-- Name: idx_checkout_sessions_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_sessions_expires ON public.checkout_sessions USING btree (expires_at);


--
-- TOC entry 3799 (class 1259 OID 49310)
-- Name: idx_checkout_sessions_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_sessions_status ON public.checkout_sessions USING btree (status);


--
-- TOC entry 3800 (class 1259 OID 49309)
-- Name: idx_checkout_sessions_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkout_sessions_user ON public.checkout_sessions USING btree (user_id);


--
-- TOC entry 3705 (class 1259 OID 25218)
-- Name: idx_images_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_images_product ON public.product_images USING btree (product_id);


--
-- TOC entry 3805 (class 1259 OID 49316)
-- Name: idx_inventory_reservations_checkout; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inventory_reservations_checkout ON public.inventory_reservations USING btree (checkout_session_id);


--
-- TOC entry 3806 (class 1259 OID 49317)
-- Name: idx_inventory_reservations_status_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inventory_reservations_status_expires ON public.inventory_reservations USING btree (status, expires_at);


--
-- TOC entry 3807 (class 1259 OID 49315)
-- Name: idx_inventory_reservations_variant_status_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inventory_reservations_variant_status_expires ON public.inventory_reservations USING btree (product_variant_id, status, expires_at);


--
-- TOC entry 3789 (class 1259 OID 25237)
-- Name: idx_logs_action; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_logs_action ON public.activity_logs USING btree (action);


--
-- TOC entry 3790 (class 1259 OID 25236)
-- Name: idx_logs_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_logs_created ON public.activity_logs USING btree (created_at);


--
-- TOC entry 3791 (class 1259 OID 25235)
-- Name: idx_logs_entity; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_logs_entity ON public.activity_logs USING btree (entity_type, entity_id);


--
-- TOC entry 3792 (class 1259 OID 25234)
-- Name: idx_logs_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_logs_user ON public.activity_logs USING btree (user_id);


--
-- TOC entry 3731 (class 1259 OID 25224)
-- Name: idx_order_items_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_order ON public.order_items USING btree (order_id);


--
-- TOC entry 3830 (class 1259 OID 57364)
-- Name: idx_order_status_history_order_time_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_status_history_order_time_id ON public.order_status_histories USING btree (order_id, created_at, id);


--
-- TOC entry 3722 (class 1259 OID 25223)
-- Name: idx_orders_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_code ON public.orders USING btree (order_code);


--
-- TOC entry 3723 (class 1259 OID 25222)
-- Name: idx_orders_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_created ON public.orders USING btree (created_at);


--
-- TOC entry 3724 (class 1259 OID 25220)
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- TOC entry 3725 (class 1259 OID 25219)
-- Name: idx_orders_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_user ON public.orders USING btree (user_id);


--
-- TOC entry 3726 (class 1259 OID 25221)
-- Name: idx_orders_user_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_user_status ON public.orders USING btree (user_id, status);


--
-- TOC entry 3823 (class 1259 OID 49321)
-- Name: idx_payment_attempts_checkout; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_attempts_checkout ON public.payment_attempts USING btree (checkout_session_id);


--
-- TOC entry 3824 (class 1259 OID 49323)
-- Name: idx_payment_attempts_gateway_transaction; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_attempts_gateway_transaction ON public.payment_attempts USING btree (gateway_transaction_id) WHERE (gateway_transaction_id IS NOT NULL);


--
-- TOC entry 3825 (class 1259 OID 49322)
-- Name: idx_payment_attempts_status_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_attempts_status_expires ON public.payment_attempts USING btree (status, expires_at);


--
-- TOC entry 3734 (class 1259 OID 25225)
-- Name: idx_payments_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_order ON public.payments USING btree (order_id);


--
-- TOC entry 3735 (class 1259 OID 25226)
-- Name: idx_payments_transaction; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_transaction ON public.payments USING btree (transaction_id) WHERE (transaction_id IS NOT NULL);


--
-- TOC entry 3685 (class 1259 OID 25212)
-- Name: idx_products_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_active ON public.products USING btree (is_active) WHERE (deleted_at IS NULL);


--
-- TOC entry 3686 (class 1259 OID 25211)
-- Name: idx_products_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_category ON public.products USING btree (category_id);


--
-- TOC entry 3687 (class 1259 OID 25213)
-- Name: idx_products_featured; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_featured ON public.products USING btree (is_featured) WHERE ((is_active = true) AND (deleted_at IS NULL));


--
-- TOC entry 3688 (class 1259 OID 25210)
-- Name: idx_products_name_trgm; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_name_trgm ON public.products USING gin (name public.gin_trgm_ops);


--
-- TOC entry 3689 (class 1259 OID 25214)
-- Name: idx_products_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_slug ON public.products USING btree (slug);


--
-- TOC entry 3738 (class 1259 OID 25227)
-- Name: idx_reviews_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reviews_product ON public.reviews USING btree (product_id, is_approved);


--
-- TOC entry 3739 (class 1259 OID 25228)
-- Name: idx_reviews_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reviews_user ON public.reviews USING btree (user_id);


--
-- TOC entry 3669 (class 1259 OID 25204)
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- TOC entry 3670 (class 1259 OID 25206)
-- Name: idx_users_membership; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_membership ON public.users USING btree (membership_tier_id);


--
-- TOC entry 3671 (class 1259 OID 25205)
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_role ON public.users USING btree (role) WHERE (deleted_at IS NULL);


--
-- TOC entry 3694 (class 1259 OID 25215)
-- Name: idx_variants_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_variants_product ON public.product_variants USING btree (product_id);


--
-- TOC entry 3695 (class 1259 OID 25216)
-- Name: idx_variants_sku; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_variants_sku ON public.product_variants USING btree (sku);


--
-- TOC entry 3696 (class 1259 OID 25217)
-- Name: idx_variants_stock_low; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_variants_stock_low ON public.product_variants USING btree (stock_quantity) WHERE (stock_quantity < 10);


--
-- TOC entry 3814 (class 1259 OID 49319)
-- Name: idx_voucher_reservations_checkout; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_voucher_reservations_checkout ON public.voucher_reservations USING btree (checkout_session_id);


--
-- TOC entry 3815 (class 1259 OID 49320)
-- Name: idx_voucher_reservations_status_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_voucher_reservations_status_expires ON public.voucher_reservations USING btree (status, expires_at);


--
-- TOC entry 3816 (class 1259 OID 49318)
-- Name: idx_voucher_reservations_voucher_status_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_voucher_reservations_voucher_status_expires ON public.voucher_reservations USING btree (voucher_id, status, expires_at);


--
-- TOC entry 3748 (class 1259 OID 25229)
-- Name: idx_wishlists_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_wishlists_user ON public.wishlists USING btree (user_id);


--
-- TOC entry 3872 (class 2620 OID 25241)
-- Name: addresses trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.addresses FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3881 (class 2620 OID 25250)
-- Name: banners trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.banners FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3882 (class 2620 OID 25251)
-- Name: blog_categories trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.blog_categories FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3883 (class 2620 OID 25252)
-- Name: blog_posts trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.blog_posts FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3880 (class 2620 OID 25249)
-- Name: cart_items trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.cart_items FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3873 (class 2620 OID 25242)
-- Name: categories trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.categories FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3876 (class 2620 OID 25245)
-- Name: collections trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.collections FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3870 (class 2620 OID 25239)
-- Name: membership_tiers trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.membership_tiers FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3878 (class 2620 OID 25247)
-- Name: orders trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3875 (class 2620 OID 25244)
-- Name: product_variants trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.product_variants FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3874 (class 2620 OID 25243)
-- Name: products trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.products FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3879 (class 2620 OID 25248)
-- Name: reviews trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.reviews FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3871 (class 2620 OID 25240)
-- Name: users trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3877 (class 2620 OID 25246)
-- Name: vouchers trigger_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON public.vouchers FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 3858 (class 2606 OID 25199)
-- Name: activity_logs activity_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- TOC entry 3834 (class 2606 OID 24805)
-- Name: addresses addresses_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 3856 (class 2606 OID 25179)
-- Name: blog_post_tags blog_post_tags_blog_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_post_tags
    ADD CONSTRAINT blog_post_tags_blog_post_id_fkey FOREIGN KEY (blog_post_id) REFERENCES public.blog_posts(id) ON DELETE CASCADE;


--
-- TOC entry 3857 (class 2606 OID 25184)
-- Name: blog_post_tags blog_post_tags_blog_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_post_tags
    ADD CONSTRAINT blog_post_tags_blog_tag_id_fkey FOREIGN KEY (blog_tag_id) REFERENCES public.blog_tags(id) ON DELETE CASCADE;


--
-- TOC entry 3854 (class 2606 OID 25153)
-- Name: blog_posts blog_posts_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- TOC entry 3855 (class 2606 OID 25148)
-- Name: blog_posts blog_posts_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blog_posts
    ADD CONSTRAINT blog_posts_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.blog_categories(id) ON DELETE SET NULL;


--
-- TOC entry 3852 (class 2606 OID 25099)
-- Name: cart_items cart_items_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE CASCADE;


--
-- TOC entry 3853 (class 2606 OID 25094)
-- Name: cart_items cart_items_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 3835 (class 2606 OID 24825)
-- Name: categories categories_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.categories(id) ON DELETE RESTRICT;


--
-- TOC entry 3861 (class 2606 OID 49232)
-- Name: checkout_session_items checkout_session_items_checkout_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_session_items
    ADD CONSTRAINT checkout_session_items_checkout_session_id_fkey FOREIGN KEY (checkout_session_id) REFERENCES public.checkout_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 3862 (class 2606 OID 49237)
-- Name: checkout_session_items checkout_session_items_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_session_items
    ADD CONSTRAINT checkout_session_items_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE RESTRICT;


--
-- TOC entry 3859 (class 2606 OID 49211)
-- Name: checkout_sessions checkout_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_sessions
    ADD CONSTRAINT checkout_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- TOC entry 3860 (class 2606 OID 49216)
-- Name: checkout_sessions checkout_sessions_voucher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkout_sessions
    ADD CONSTRAINT checkout_sessions_voucher_id_fkey FOREIGN KEY (voucher_id) REFERENCES public.vouchers(id) ON DELETE SET NULL;


--
-- TOC entry 3839 (class 2606 OID 24919)
-- Name: collection_products collection_products_collection_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products
    ADD CONSTRAINT collection_products_collection_id_fkey FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE;


--
-- TOC entry 3840 (class 2606 OID 24924)
-- Name: collection_products collection_products_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_products
    ADD CONSTRAINT collection_products_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- TOC entry 3863 (class 2606 OID 49255)
-- Name: inventory_reservations inventory_reservations_checkout_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations
    ADD CONSTRAINT inventory_reservations_checkout_session_id_fkey FOREIGN KEY (checkout_session_id) REFERENCES public.checkout_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 3864 (class 2606 OID 49260)
-- Name: inventory_reservations inventory_reservations_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_reservations
    ADD CONSTRAINT inventory_reservations_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE RESTRICT;


--
-- TOC entry 3843 (class 2606 OID 24987)
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- TOC entry 3844 (class 2606 OID 24992)
-- Name: order_items order_items_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE RESTRICT;


--
-- TOC entry 3868 (class 2606 OID 57359)
-- Name: order_status_histories order_status_histories_changed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_status_histories
    ADD CONSTRAINT order_status_histories_changed_by_fkey FOREIGN KEY (changed_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- TOC entry 3869 (class 2606 OID 57354)
-- Name: order_status_histories order_status_histories_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_status_histories
    ADD CONSTRAINT order_status_histories_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- TOC entry 3841 (class 2606 OID 24967)
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- TOC entry 3842 (class 2606 OID 24972)
-- Name: orders orders_voucher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_voucher_id_fkey FOREIGN KEY (voucher_id) REFERENCES public.vouchers(id) ON DELETE SET NULL;


--
-- TOC entry 3867 (class 2606 OID 49304)
-- Name: payment_attempts payment_attempts_checkout_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_attempts
    ADD CONSTRAINT payment_attempts_checkout_session_id_fkey FOREIGN KEY (checkout_session_id) REFERENCES public.checkout_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 3845 (class 2606 OID 25009)
-- Name: payments payments_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- TOC entry 3838 (class 2606 OID 24890)
-- Name: product_images product_images_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- TOC entry 3837 (class 2606 OID 24873)
-- Name: product_variants product_variants_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- TOC entry 3836 (class 2606 OID 24851)
-- Name: products products_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE RESTRICT;


--
-- TOC entry 3849 (class 2606 OID 25056)
-- Name: review_images review_images_review_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_images
    ADD CONSTRAINT review_images_review_id_fkey FOREIGN KEY (review_id) REFERENCES public.reviews(id) ON DELETE CASCADE;


--
-- TOC entry 3846 (class 2606 OID 25040)
-- Name: reviews reviews_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- TOC entry 3847 (class 2606 OID 25035)
-- Name: reviews reviews_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- TOC entry 3848 (class 2606 OID 25030)
-- Name: reviews reviews_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 3833 (class 2606 OID 24788)
-- Name: users users_membership_tier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_membership_tier_id_fkey FOREIGN KEY (membership_tier_id) REFERENCES public.membership_tiers(id) ON DELETE SET NULL;


--
-- TOC entry 3865 (class 2606 OID 49279)
-- Name: voucher_reservations voucher_reservations_checkout_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations
    ADD CONSTRAINT voucher_reservations_checkout_session_id_fkey FOREIGN KEY (checkout_session_id) REFERENCES public.checkout_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 3866 (class 2606 OID 49284)
-- Name: voucher_reservations voucher_reservations_voucher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voucher_reservations
    ADD CONSTRAINT voucher_reservations_voucher_id_fkey FOREIGN KEY (voucher_id) REFERENCES public.vouchers(id) ON DELETE RESTRICT;


--
-- TOC entry 3850 (class 2606 OID 25076)
-- Name: wishlists wishlists_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists
    ADD CONSTRAINT wishlists_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- TOC entry 3851 (class 2606 OID 25071)
-- Name: wishlists wishlists_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlists
    ADD CONSTRAINT wishlists_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


-- Completed on 2026-06-25 23:31:09

--
-- PostgreSQL database dump complete
--

\unrestrict TMxt5rFxams6CHRCfeGaf2L1d7cSrqEpFhuGdK6BIJfW47kDaxZ3yxUyHGsm4LZ

